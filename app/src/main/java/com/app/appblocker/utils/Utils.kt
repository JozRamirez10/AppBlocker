package com.app.appblocker.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.app.appblocker.R
import com.app.appblocker.enums.DaysOfWeek
import com.app.appblocker.services.AppBlockerAccessibilityService
import org.threeten.bp.LocalTime

class Utils {

    object PermissionUtils{
        fun openAccessibilitySettings(context: Context){
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }

        fun isAccessibilityServiceEnabled(context: Context) : Boolean {
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            val serviceId = context.packageName + "/" + AppBlockerAccessibilityService::class.java.name
            return enabledServices.contains(serviceId)
        }

        // --- NOTIFICATION PERMISSION (Android 13+) ---
        fun hasNotificationPermission(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true
        }
    }

    object ParseUtils{
        fun parseDays(daysString : String) : List<Int> {
            return daysString.split(",")
                .mapNotNull { DaysOfWeek.fromString(it.trim())?.intValue }
        }

        fun parseHour(hourString : String) : Int {
            return LocalTime.parse(hourString).hour
        }
    }

    object ToasUtils {
        fun showToast(
            context: Context,
            text : String
        ){
            Toast.makeText(
                context,
                text,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    object DialogUtils {
        fun showConfirmDialog(
            context: Context,
            title: String,
            message: String,
            positiveText: String = "Yes",
            negativeText: String = "No",
            onConfirm: () -> Unit,
            onCancel: ( () -> Unit ) ? = null
        ){
            AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveText) { _, _ -> onConfirm()}
                .setNegativeButton(negativeText) { dialog, _ ->
                    onCancel?.invoke()
                    dialog.dismiss()
                }
                .show()
        }
    }

    object SwipeToDeleteHelper {

        fun <T> attachToRecyclerView(
            recyclerView: RecyclerView,
            getItem: (Int) -> T,
            onDeleteConfirmed: (T) -> Unit,
            context: Context,
            title: String,
            message: String
        ) {
            val callback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT){
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean = false

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.adapterPosition
                    val item = getItem(position)

                    DialogUtils.showConfirmDialog(
                        context = context,
                        title = title,
                        message = message,
                        onConfirm = { onDeleteConfirmed(item) },
                        onCancel = { recyclerView.adapter?.notifyItemChanged(position) }
                    )

                }

                override fun onChildDraw(
                    c: Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean
                ) {
                    super.onChildDraw(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )

                    val itemView = viewHolder.itemView
                    val paint = Paint().apply { color = Color.RED }

                    if(dX < 0){
                        c.drawRect(
                            itemView.right.toFloat() + dX,
                            itemView.top.toFloat(),
                            itemView.right.toFloat(),
                            itemView.bottom.toFloat(),
                            paint
                        )

                        val icon = ContextCompat.getDrawable(context, R.drawable.ic_delete)
                        icon?.let {
                            it.setTint(Color.WHITE)
                            val iconMargin = (itemView.height - it.intrinsicHeight) / 2
                            val iconTop = itemView.top + (itemView.height - it.intrinsicHeight) / 2
                            val iconLeft = itemView.right - iconMargin - it.intrinsicWidth
                            val iconRight = itemView.right - iconMargin
                            val iconBottom = iconTop + it.intrinsicHeight
                            it.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                            it.draw(c)
                        }
                    }
                }
            }
            ItemTouchHelper(callback).attachToRecyclerView(recyclerView)
        }
    }
}