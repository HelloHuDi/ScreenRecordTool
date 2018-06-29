package com.hd.screenrecordtool.custom

import android.app.AlertDialog
import android.content.Context
import android.os.Environment
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import java.io.File
import java.io.IOException

/**
 * Builder class for a folder picker dialog.
 */
class FolderPickerDialog(context: Context, root: File) : AlertDialog.Builder(context) {

    private val mAdapter: ArrayAdapter<String>
    private var mAlert: AlertDialog? = null

    private var mRoot: File? = null

    init {
        mRoot = root

        mAdapter = ArrayAdapter(getContext(), android.R.layout.simple_list_item_1)

        update()

        val list = ListView(getContext())
        list.adapter = mAdapter
        list.setOnItemClickListener { parentAdapterView, _, position, _ ->
            val dir = parentAdapterView.getItemAtPosition(position) as String
            val parent = mRoot!!.parentFile
            mRoot = if (dir == ".." && parent != null) {
                parent
            } else {
                File(mRoot, dir)
            }
            update()
        }

        setView(list)
    }

    override fun create(): AlertDialog? {
        if (mAlert != null) throw RuntimeException("Cannot reuse builder")
        mAlert = super.create()
        return mAlert
    }

    private fun update() {
        mRoot = try {
            File(mRoot!!.canonicalPath)
        } catch (e: IOException) {
            Log.d("tag","Directory root is incorrect, fixing to external storage.")
            Environment.getExternalStorageDirectory()
        }

        if (mAlert != null) {
            mAlert!!.setTitle(mRoot!!.absolutePath)
        } else {
            setTitle(mRoot!!.absolutePath)
        }

        mAdapter.clear()
        var dirs: Array<String>? = mRoot!!.list { dir, filename ->
            val file = File(dir, filename)
            file.isDirectory && !file.isHidden
        }
        if (dirs == null) {
            Log.d("tag","Unable to receive dirs list, no Access rights?")
            Log.d("tag","Unable to fix, continue with empty list")
            dirs = arrayOf()
        }
        mAdapter.add("..")
        mAdapter.addAll(*dirs)
    }

    fun setSelectedButton(textId: Int, listener: OnSelectedListener): AlertDialog.Builder {
        return setPositiveButton(textId) { _, _ -> listener.onSelected(mRoot!!.absolutePath) }
    }

    interface OnSelectedListener {
        fun onSelected(path: String)
    }

}
