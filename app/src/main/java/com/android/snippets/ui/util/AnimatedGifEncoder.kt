package com.android.snippets.ui.util

import android.graphics.Bitmap
import java.io.IOException
import java.io.OutputStream

/**
 * AnimatedGifEncoder - Pure Kotlin implementation for encoding Bitmaps into GIF format.
 */
class AnimatedGifEncoder {
    private var width = 0
    private var height = 0
    private var transparent: Int? = null
    private var transIndex = 0
    private var repeat = 0 // 0 = repeat forever
    private var delay = 100 // frame delay in ms (100ms = 10fps)
    private var started = false
    private var out: OutputStream? = null
    private var image: Bitmap? = null
    private var pixels: ByteArray = byteArrayOf()
    private var indexedPixels: ByteArray = byteArrayOf()
    private var colorDepth = 8
    private var colorTab: ByteArray = byteArrayOf()
    private var usedEntry = BooleanArray(256)
    private var palSize = 7
    private var dispose = -1
    private var closeStream = false
    private var firstFrame = true
    private var sizeSet = false
    private var sample = 10 // default sample interval for quantizer

    fun setDelay(ms: Int) {
        delay = ms
    }

    fun setRepeat(iter: Int) {
        if (iter >= 0) {
            repeat = iter
        }
    }

    fun setQuality(quality: Int) {
        var q = quality
        if (q < 1) q = 1
        sample = q
    }

    fun setSize(w: Int, h: Int) {
        if (started && !firstFrame) return
        width = w
        height = h
        if (width < 1) width = 320
        if (height < 1) height = 240
        sizeSet = true
    }

    fun start(os: OutputStream?): Boolean {
        if (os == null) return false
        var ok = true
        closeStream = false
        out = os
        try {
            writeString("GIF89a") // header
        } catch (e: IOException) {
            ok = false
        }
        started = ok
        return started
    }

    fun addFrame(im: Bitmap?): Boolean {
        if (im == null || !started) return false
        var ok = true
        try {
            if (!sizeSet) {
                setSize(im.width, im.height)
            }
            image = im
            getImagePixels()
            analyzePixels()
            if (firstFrame) {
                writeLSD()
                writePalette()
                if (repeat >= 0) {
                    writeNetscapeExt()
                }
            }
            writeGraphicCtrlExt()
            writeImageDesc()
            if (!firstFrame) {
                writePalette()
            }
            writePixels()
            firstFrame = false
        } catch (e: IOException) {
            ok = false
        }
        return ok
    }

    fun finish(): Boolean {
        if (!started) return false
        var ok = true
        started = false
        try {
            out?.write(0x3b) // gif trailer
            out?.flush()
            if (closeStream) {
                out?.close()
            }
        } catch (e: IOException) {
            ok = false
        }

        // reset state
        transIndex = 0
        out = null
        image = null
        pixels = byteArrayOf()
        indexedPixels = byteArrayOf()
        colorTab = byteArrayOf()
        closeStream = false
        firstFrame = true
        return ok
    }

    private fun getImagePixels() {
        val w = image!!.width
        val h = image!!.height
        if (w != width || h != height) {
            val temp = Bitmap.createScaledBitmap(image!!, width, height, true)
            image = temp
        }
        val intArray = IntArray(width * height)
        image!!.getPixels(intArray, 0, width, 0, 0, width, height)
        pixels = ByteArray(intArray.size * 3)
        var p = 0
        for (i in intArray.indices) {
            val c = intArray[i]
            pixels[p++] = (c shr 16 and 0xff).toByte() // R
            pixels[p++] = (c shr 8 and 0xff).toByte()  // G
            pixels[p++] = (c and 0xff).toByte()        // B
        }
    }

    private fun analyzePixels() {
        val len = pixels.size
        val nPix = len / 3
        indexedPixels = ByteArray(nPix)
        val nq = NeuQuant(pixels, len, sample)
        colorTab = nq.process()
        // map image pixels to new palette
        var k = 0
        for (i in 0 until nPix) {
            val index = nq.map(
                pixels[k++].toInt() and 0xff,
                pixels[k++].toInt() and 0xff,
                pixels[k++].toInt() and 0xff
            )
            usedEntry[index] = true
            indexedPixels[i] = index.toByte()
        }
        pixels = byteArrayOf()
        colorDepth = 8
        palSize = 7
    }

    private fun writeGraphicCtrlExt() {
        out?.write(0x21) // extension introducer
        out?.write(0xf9) // GCE label
        out?.write(4)    // data block size
        val transp = if (transparent == null) 0 else 1
        var disp = if (dispose >= 0) dispose and 7 else 0
        disp = disp shl 2
        out?.write(0 or disp or 0 or transp) // packed fields
        writeShort(delay / 10) // delay in 1/100ths of a second
        out?.write(transIndex)
        out?.write(0) // block terminator
    }

    private fun writeImageDesc() {
        out?.write(0x2c) // image separator
        writeShort(0)    // image left
        writeShort(0)    // image top
        writeShort(width)
        writeShort(height)
        if (firstFrame) {
            out?.write(0)
        } else {
            out?.write(0x80 or 0 or 0 or 0 or palSize) // packed fields
        }
    }

    private fun writeLSD() {
        writeShort(width)
        writeShort(height)
        out?.write(0x80 or 0x70 or 0x00 or palSize)
        out?.write(0) // background color index
        out?.write(0) // pixel aspect ratio
    }

    private fun writePalette() {
        out?.write(colorTab, 0, colorTab.size)
        val n = 3 * 256 - colorTab.size
        for (i in 0 until n) {
            out?.write(0)
        }
    }

    private fun writeNetscapeExt() {
        out?.write(0x21) // extension introducer
        out?.write(0xff) // app extension label
        out?.write(11)   // block size
        writeString("NETSCAPE2.0")
        out?.write(3)    // sub-block size
        out?.write(1)    // loop sub-block id
        writeShort(repeat) // loop count (0 = infinite)
        out?.write(0)    // block terminator
    }

    private fun writePixels() {
        val encoder = LZWEncoder(width, height, indexedPixels, colorDepth)
        encoder.encode(out)
    }

    private fun writeShort(value: Int) {
        out?.write(value and 0xff)
        out?.write(value shr 8 and 0xff)
    }

    private fun writeString(s: String) {
        for (i in s.indices) {
            out?.write(s[i].code)
        }
    }
}

class LZWEncoder(
    private val imgW: Int,
    private val imgH: Int,
    private val pixAry: ByteArray,
    private val initCodeSize: Int
) {
    private val BITS = 12
    private val HSIZE = 5003

    private var nBits = 0
    private var maxcode = 0
    private var htab = IntArray(HSIZE)
    private var codetab = IntArray(HSIZE)
    private var freeEnt = 0
    private var clearFlg = false

    private var gInitBits = 0
    private var clearCode = 0
    private var eofCode = 0

    private var curAccum = 0
    private var curCount = 0

    private var accum = ByteArray(256)

    private fun charOut(c: Byte, outs: OutputStream) {
        accum[curCount++] = c
        if (curCount >= 254) flushChar(outs)
    }

    private fun clBlock(outs: OutputStream) {
        clHash(HSIZE)
        freeEnt = clearCode + 2
        clearFlg = true
        output(clearCode, outs)
    }

    private fun clHash(hsize: Int) {
        for (i in 0 until hsize) htab[i] = -1
    }

    fun encode(os: OutputStream?) {
        if (os == null) return
        os.write(initCodeSize)
        val remaining = imgW * imgH
        var curPixel = 0

        gInitBits = initCodeSize
        clearFlg = false
        nBits = gInitBits
        maxcode = maxCode(nBits)

        clearCode = 1 shl (initCodeSize - 1)
        eofCode = clearCode + 1
        freeEnt = clearCode + 2

        curAccum = 0
        curCount = 0

        var ent = pixAry[curPixel++].toInt() and 0xff
        var hshift = 0
        var fcode = HSIZE
        while (fcode < 65536) {
            hshift++
            fcode *= 2
        }
        hshift = 8 - hshift

        val hsizeReg = HSIZE
        clHash(hsizeReg)

        output(clearCode, os)

        while (curPixel < remaining) {
            val c = pixAry[curPixel++].toInt() and 0xff
            fcode = (c shl BITS) + ent
            var i = (c shl hshift) xor ent
            if (htab[i] == fcode) {
                ent = codetab[i]
                continue
            } else if (htab[i] >= 0) {
                var disp = hsizeReg - i
                if (i == 0) disp = 1
                do {
                    i -= disp
                    if (i < 0) i += hsizeReg
                    if (htab[i] == fcode) {
                        ent = codetab[i]
                        break
                    }
                } while (htab[i] >= 0)
                if (htab[i] == fcode) continue
            }
            output(ent, os)
            ent = c
            if (freeEnt < 1 shl BITS) {
                codetab[i] = freeEnt++
                htab[i] = fcode
            } else {
                clBlock(os)
            }
        }
        output(ent, os)
        output(eofCode, os)
    }

    private fun flushChar(outs: OutputStream) {
        if (curCount > 0) {
            outs.write(curCount)
            outs.write(accum, 0, curCount)
            curCount = 0
        }
    }

    private fun maxCode(nBits: Int): Int = (1 shl nBits) - 1

    private fun output(code: Int, outs: OutputStream) {
        curAccum = curAccum or (code shl curCount)
        curCount += nBits

        while (curCount >= 8) {
            charOut((curAccum and 0xff).toByte(), outs)
            curAccum = curAccum shr 8
            curCount -= 8
        }

        if (freeEnt > maxcode || clearFlg) {
            if (clearFlg) {
                nBits = gInitBits
                maxcode = maxCode(nBits)
                clearFlg = false
            } else {
                nBits++
                maxcode = if (nBits == BITS) 1 shl BITS else maxCode(nBits)
            }
        }

        if (code == eofCode) {
            while (curCount > 0) {
                charOut((curAccum and 0xff).toByte(), outs)
                curAccum = curAccum shr 8
                curCount -= 8
            }
            flushChar(outs)
        }
    }
}

class NeuQuant(thepic: ByteArray, len: Int, samplefac: Int) {
    private val netsize = 256
    private val prime1 = 499
    private val prime2 = 491
    private val prime3 = 487
    private val prime4 = 503
    private val minpicturebytes = 3 * prime4

    private val maxnetpos = netsize - 1
    private val netbiasshift = 4
    private val ncycles = 10

    private val intbiasshift = 16
    private val intbias = 1 shl intbiasshift
    private val gammashift = 10
    private val gamma = 1 shl gammashift
    private val betashift = 10
    private val beta = intbias shr betashift
    private val betagamma = intbias shl (gammashift - betashift)

    private val initrad = netsize shr 3
    private val radiusbiasshift = 6
    private val radiusbias = 1 shl radiusbiasshift
    private val initradius = initrad * radiusbias
    private val radiusdec = 30

    private val alphabiasshift = 10
    private val initalpha = 1 shl alphabiasshift
    private var alphadec = 0

    private val radbiasshift = 8
    private val radbias = 1 shl radbiasshift
    private val alpharadbshift = alphabiasshift + radbiasshift
    private val alpharadbias = 1 shl alpharadbshift

    private val thepicture: ByteArray = thepic
    private val lengthcount: Int = len
    private var samplefactor: Int = samplefac

    private val network = Array(netsize) { FloatArray(4) }
    private val netindex = IntArray(256)
    private val bias = IntArray(netsize)
    private val freq = IntArray(netsize)
    private val radpower = IntArray(initrad)

    init {
        for (i in 0 until netsize) {
            network[i] = floatArrayOf(
                (i shl (netbiasshift + 8)) / netsize.toFloat(),
                (i shl (netbiasshift + 8)) / netsize.toFloat(),
                (i shl (netbiasshift + 8)) / netsize.toFloat(),
                0f
            )
            freq[i] = intbias / netsize
            bias[i] = 0
        }
    }

    fun colorMap(): ByteArray {
        val map = ByteArray(3 * netsize)
        val index = IntArray(netsize)
        for (i in 0 until netsize) index[network[i][3].toInt()] = i
        var k = 0
        for (i in 0 until netsize) {
            val j = index[i]
            map[k++] = network[j][0].toInt().toByte()
            map[k++] = network[j][1].toInt().toByte()
            map[k++] = network[j][2].toInt().toByte()
        }
        return map
    }

    fun map(r: Int, g: Int, b: Int): Int {
        var bestd = 1000
        var best = -1
        var i = netindex[g]
        var j = i - 1

        while (i < netsize || j >= 0) {
            if (i < netsize) {
                val p = network[i]
                var dist = p[1].toInt() - g
                if (dist >= bestd) i = netsize
                else {
                    i++
                    if (dist < 0) dist = -dist
                    var a = p[0].toInt() - r
                    if (a < 0) a = -a
                    dist += a
                    if (dist < bestd) {
                        a = p[2].toInt() - b
                        if (a < 0) a = -a
                        dist += a
                        if (dist < bestd) {
                            bestd = dist
                            best = p[3].toInt()
                        }
                    }
                }
            }
            if (j >= 0) {
                val p = network[j]
                var dist = g - p[1].toInt()
                if (dist >= bestd) j = -1
                else {
                    j--
                    if (dist < 0) dist = -dist
                    var a = p[0].toInt() - r
                    if (a < 0) a = -a
                    dist += a
                    if (dist < bestd) {
                        a = p[2].toInt() - b
                        if (a < 0) a = -a
                        dist += a
                        if (dist < bestd) {
                            bestd = dist
                            best = p[3].toInt()
                        }
                    }
                }
            }
        }
        return best
    }

    fun process(): ByteArray {
        learn()
        unbiasnet()
        inxbuild()
        return colorMap()
    }

    private fun unbiasnet() {
        for (i in 0 until netsize) {
            network[i][0] = (network[i][0].toInt() shr netbiasshift).toFloat()
            network[i][1] = (network[i][1].toInt() shr netbiasshift).toFloat()
            network[i][2] = (network[i][2].toInt() shr netbiasshift).toFloat()
            network[i][3] = i.toFloat()
        }
    }

    private fun inxbuild() {
        var previouscol = 0
        var startpos = 0
        for (i in 0 until netsize) {
            val p = network[i]
            var smallpos = i
            var smallval = p[1].toInt()
            for (j in i + 1 until netsize) {
                val q = network[j]
                if (q[1].toInt() < smallval) {
                    smallpos = j
                    smallval = q[1].toInt()
                }
            }
            val q = network[smallpos]
            if (i != smallpos) {
                var temp = p[0]; p[0] = q[0]; q[0] = temp
                temp = p[1]; p[1] = q[1]; q[1] = temp
                temp = p[2]; p[2] = q[2]; q[2] = temp
                temp = p[3]; p[3] = q[3]; q[3] = temp
            }
            if (smallval != previouscol) {
                netindex[previouscol] = (startpos + i) shr 1
                for (j in previouscol + 1 until smallval) netindex[j] = i
                previouscol = smallval
                startpos = i
            }
        }
        netindex[previouscol] = (startpos + maxnetpos) shr 1
        for (j in previouscol + 1 until 256) netindex[j] = maxnetpos
    }

    private fun learn() {
        var i: Int
        val step: Int
        var radius: Int
        var alpha: Int
        val delta: Int
        val samplepixels: Int
        val p: ByteArray
        var pix: Int
        val lim: Int

        if (lengthcount < minpicturebytes) samplefactor = 1
        alphadec = 30 + (samplefactor - 1) / 3
        p = thepicture
        pix = 0
        lim = lengthcount
        samplepixels = lengthcount / (3 * samplefactor)
        delta = samplepixels / ncycles
        alpha = initalpha
        radius = initradius

        var rad = radius shr radiusbiasshift
        if (rad <= 1) radius = 0
        for (k in 0 until rad) {
            radpower[k] = alpha * (((rad * rad - k * k) * radbias) / (rad * rad))
        }

        step = if (lengthcount < minpicturebytes) 3
        else if (lengthcount % prime1 != 0) 3 * prime1
        else if (lengthcount % prime2 != 0) 3 * prime2
        else if (lengthcount % prime3 != 0) 3 * prime3
        else 3 * prime4

        i = 0
        while (i < samplepixels) {
            val r = (p[pix + 0].toInt() and 0xff) shl netbiasshift
            val g = (p[pix + 1].toInt() and 0xff) shl netbiasshift
            val b = (p[pix + 2].toInt() and 0xff) shl netbiasshift
            val j = contest(r, g, b)

            altersingle(alpha, j, r, g, b)
            if (rad != 0) alterneigh(rad, j, r, g, b)

            pix += step
            if (pix >= lim) pix -= lengthcount

            i++
            if (delta == 0) continue
            if (i % delta == 0) {
                alpha -= alpha / alphadec
                radius -= radius / radiusdec
                rad = radius shr radiusbiasshift
                if (rad <= 1) radius = 0
                for (k in 0 until rad) {
                    radpower[k] = alpha * (((rad * rad - k * k) * radbias) / (rad * rad))
                }
            }
        }
    }

    private fun altersingle(alpha: Int, i: Int, r: Int, g: Int, b: Int) {
        val n = network[i]
        n[0] -= (alpha * (n[0] - r)) / initalpha
        n[1] -= (alpha * (n[1] - g)) / initalpha
        n[2] -= (alpha * (n[2] - b)) / initalpha
    }

    private fun alterneigh(rad: Int, i: Int, r: Int, g: Int, b: Int) {
        var lo = i - rad
        if (lo < -1) lo = -1
        var hi = i + rad
        if (hi > netsize) hi = netsize

        var j = i + 1
        var k = i - 1
        var m = 1
        while (j < hi || k > lo) {
            val a = radpower[m++]
            if (j < hi) {
                val p = network[j++]
                p[0] -= (a * (p[0] - r)) / alpharadbias
                p[1] -= (a * (p[1] - g)) / alpharadbias
                p[2] -= (a * (p[2] - b)) / alpharadbias
            }
            if (k > lo) {
                val p = network[k--]
                p[0] -= (a * (p[0] - r)) / alpharadbias
                p[1] -= (a * (p[1] - g)) / alpharadbias
                p[2] -= (a * (p[2] - b)) / alpharadbias
            }
        }
    }

    private fun contest(r: Int, g: Int, b: Int): Int {
        var bestd = Int.MAX_VALUE
        var bestbiasd = bestd
        var bestpos = -1
        var bestbiaspos = bestpos

        for (i in 0 until netsize) {
            val n = network[i]
            var dist = n[0].toInt() - r
            if (dist < 0) dist = -dist
            var a = n[1].toInt() - g
            if (a < 0) a = -a
            dist += a
            a = n[2].toInt() - b
            if (a < 0) a = -a
            dist += a

            if (dist < bestd) {
                bestd = dist
                bestpos = i
            }
            val biasdist = dist - (bias[i] shr (intbiasshift - netbiasshift))
            if (biasdist < bestbiasd) {
                bestbiasd = biasdist
                bestbiaspos = i
            }
            val betafreq = freq[i] shr betashift
            freq[i] -= betafreq
            bias[i] += betafreq shl gammashift
        }
        freq[bestpos] += beta
        bias[bestpos] -= betagamma
        return bestbiaspos
    }
}
