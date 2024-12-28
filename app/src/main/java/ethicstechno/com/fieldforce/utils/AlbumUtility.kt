package ethicstechno.com.fieldforce.utils

import android.app.Activity
import com.yanzhenjie.album.Album
import java.io.File

class AlbumUtility(private val activity: Activity, private val isCamera: Boolean = true) {

    fun openAlbumAndHandleImageSelection(
        onImageSelected: (File) -> Unit,
        onError: (String) -> Unit
    ) {
        Album.image(activity)
            .singleChoice()
            .columnCount(3)
            .camera(isCamera)
            .onResult { result ->
                if (result != null && result.isNotEmpty()) {
                    val selectedFile = File(result[0].path)
                    onImageSelected(selectedFile)
                } else {
                    onError("No image selected.")
                }
            }
            .onCancel { }
            .start()
    }

    fun openAlbumAndHandleImageMultipleSelection(
        onImagesSelected: (List<File>) -> Unit,
        onError: (String) -> Unit
    ) {
        Album.image(activity)
            .multipleChoice()
            .selectCount(4)
            .columnCount(3)
            .camera(isCamera)
            .onResult { result ->
                if (result != null && result.isNotEmpty()) {
                    val selectedFiles = result.map { File(it.path) }
                    onImagesSelected(selectedFiles)
                } else {
                    onError("No image selected.")
                }
            }
            .onCancel { }
            .start()
    }

    fun openAlbumAndHandleCameraSelection(
        onImageSelected: (File) -> Unit,
        onError: (String) -> Unit
    ) {
        Album.camera(activity)
            .image()
            .onResult { result ->
                if (result != null && result.isNotEmpty()) {
                    val selectedFile = File(result)
                    onImageSelected(selectedFile)
                } else {
                    onError("No image selected.")
                }
            }
            .onCancel { }
            .start()
    }
}
