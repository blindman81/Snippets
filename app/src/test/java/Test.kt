import androidx.compose.animation.SharedTransitionScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.animation.ExperimentalSharedTransitionApi
@OptIn(ExperimentalSharedTransitionApi::class)
fun test() { val mode = SharedTransitionScope.ResizeMode.scaleToBounds(ContentScale.Crop) }
