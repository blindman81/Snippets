package com.android.snippets.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.CapturedAnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.android.snippets.model.Photo
import com.android.snippets.ui.theme.GoogleSansFlexRound
import com.android.snippets.ui.util.Motion
import com.android.snippets.viewmodel.SnippetsViewModel

@Composable
fun LibraryModalsHost(
    viewModel: SnippetsViewModel,
    deletingBox: String? = null,
    onDismissDeleteBox: () -> Unit = {},
    onConfirmDeleteBox: (String) -> Unit = {},
    modalPhoto: Photo? = null,
    showAddModal: Boolean = false,
    onCloseAddModal: () -> Unit = {},
    showSnippetsModal: Boolean = false,
    onCloseSnippetsModal: () -> Unit = {},
    showDeleteModal: Boolean = false,
    onCloseDeleteModal: () -> Unit = {},
    showRateModal: Boolean = false,
    onCloseRateModal: () -> Unit = {},
    showLinkModal: Boolean = false,
    onCloseLinkModal: () -> Unit = {},
    showNameModal: Boolean = false,
    onCloseNameModal: () -> Unit = {},
    modifier: Modifier = Modifier,
    hazeState: dev.chrisbanes.haze.HazeState? = LocalHazeState.current
) {
    val view = LocalView.current

    deletingBox?.let { box ->
        val boxDisplayName = com.android.snippets.logic.BoxManager.getBoxDisplayName(
            box = box,
            userBoxes = viewModel.userBoxes
        )
        BlurredAlertDialog(
            onDismissRequest = onDismissDeleteBox,
            onConfirm = {
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                onConfirmDeleteBox(box)
            },
            title = {
                Text(
                    text = "Delete $boxDisplayName?",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = GoogleSansFlexRound,
                        letterSpacing = (-0.5).sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    fontFamily = GoogleSansFlexRound
                )
            },
            text = {
                Text(
                    text = "Your photos will still be in Box.",
                    style = MaterialTheme.typography.bodyLarge.copy(fontFamily = GoogleSansFlexRound),
                    fontFamily = GoogleSansFlexRound,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )
    }

    var lastModalPhoto by remember { mutableStateOf<Photo?>(null) }
    if (modalPhoto != null) {
        lastModalPhoto = modalPhoto
    }
    val targetPhoto = modalPhoto ?: lastModalPhoto
    val currentModalPhoto = targetPhoto?.let { p -> viewModel.photos.find { it.id == p.id } ?: p }
    val isModalActive = modalPhoto != null && (showAddModal || showSnippetsModal || showDeleteModal || showRateModal || showLinkModal || showNameModal)

    CapturedAnimatedVisibility(
        visible = isModalActive,
        enter = fadeIn(animationSpec = Motion.StandardFade),
        exit = fadeOut(animationSpec = Motion.FastFade),
        modifier = modifier.zIndex(999f),
        label = "LibraryModalHostCapturedVisibility"
    ) {
        if (currentModalPhoto != null) {
            if (showAddModal) {
                AddSnippetsModal(
                    photo = currentModalPhoto,
                    onAdd = { name -> viewModel.updateSnippets(currentModalPhoto.id, name) },
                    onClose = onCloseAddModal,
                    viewModel = viewModel
                )
            }
            if (showSnippetsModal) {
                SnippetsModal(
                    photo = currentModalPhoto,
                    onRemove = { snippet -> viewModel.removeSnippet(currentModalPhoto.id, snippet) },
                    onClose = onCloseSnippetsModal,
                    viewModel = viewModel
                )
            }
            if (showDeleteModal) {
                DeleteConfirmationModal(
                    count = 1,
                    hasPublicPhotos = currentModalPhoto.isPublic,
                    onDismiss = onCloseDeleteModal,
                    onConfirm = { unpublish ->
                        viewModel.deletePhoto(currentModalPhoto.id, unpublish = unpublish)
                        if (viewModel.activePhotoId == currentModalPhoto.id) {
                            viewModel.activePhotoId = null
                        }
                        onCloseDeleteModal()
                    }
                )
            }
            if (showRateModal) {
                Cookiestarslider(
                    initialRating = currentModalPhoto.rating,
                    onDismiss = onCloseRateModal,
                    onConfirm = { rating ->
                        viewModel.setPhotoRating(currentModalPhoto.id, rating)
                        onCloseRateModal()
                    },
                    hazeState = hazeState
                )
            }
            if (showLinkModal) {
                LocationLinkModal(
                    initialLink = currentModalPhoto.locationLink,
                    initialName = currentModalPhoto.locationName,
                    suggestions = viewModel.allUniqueLocations,
                    locationNames = viewModel.locationNameMap,
                    onDismiss = onCloseLinkModal,
                    onSave = { link, name ->
                        viewModel.updateLocationLink(currentModalPhoto.id, link.takeIf { it.isNotBlank() }, name)
                        onCloseLinkModal()
                    }
                )
            }
            if (showNameModal) {
                NamePhotoModal(
                    initialName = currentModalPhoto.name,
                    onDismiss = onCloseNameModal,
                    onSave = { name ->
                        viewModel.updatePhotoName(currentModalPhoto.id, name)
                        onCloseNameModal()
                    }
                )
            }
        }
    }
}
