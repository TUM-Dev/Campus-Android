package de.tum.`in`.tumcampusapp.utils.ui

import android.app.Dialog
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.tum.`in`.tumcampusapp.R

open class RoundedBottomSheetDialogFragment : BottomSheetDialogFragment() {

    override fun getTheme() = R.style.BottomSheetDialogTheme

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme)
    }

}
