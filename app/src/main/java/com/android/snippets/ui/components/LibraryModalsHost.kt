package com.android.snippets.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.android.snippets.model.Photo
import com.android.snippets.viewmodel.SnippetsViewModel

@Composable
fun LibraryModalsHost(
    viewModel: SnippetsViewModel,
    deletingCollection: String?,
    onDismissDeleteCollection: () -> Unit,
    onConfirmDeleteCollection: (String) -> Unit,
    modalPhoto: Photo?,
    showAddModal: Boolean,
    onCloseAddModal: () -> Unit,
    showSnippetsModal: Boolean,
    onCloseSnippetsModal: () -> Unit,
    showDeleteModal: Boolean,
    onCloseDeleteModal: () -> Unit,
    showRateModal: Boolean,
    onCloseRateModal: () -> Unit,
    showLinkModal: Boolean,
    onCloseLinkModal: () -> Unit,
    showNameModal: Boolean,
    onCloseNameModal: () -> Unit
) {
    val view = LocalView.current

    deletingCollection?.let { collection ->
        BlurredAlertDialog(
            onDismissRequest = onDismissDeleteCollection,
            onConfirm = {
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                onConfirmDeleteCollection(collection)
            },
            title = {
                Text(
                    text = "Delete collection?",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.5).sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this collection? Your photos will be saved.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )
    }

    val currentModalPhoto = modalPhoto?.let { p -> viewModel.photos.find { it.id == p.id } ?: p }
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
            RateFoodDialog(
                initialRating = currentModalPhoto.rating,
                onDismiss = onCloseRateModal,
                onConfirm = { rating ->
                    viewModel.setPhotoRating(currentModalPhoto.id, rating)
                    onCloseRateModal()
                }
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
