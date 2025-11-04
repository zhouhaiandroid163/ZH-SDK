package com.zjw.sdkdemo.ui.adapter

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.zjw.sdkdemo.R

class MyExpandableAdapter(private val context: Context, private val groupList: MutableList<Int> = mutableListOf(), private val childMap: Map<Int, List<Int>>) : BaseExpandableListAdapter() {
    constructor(context: Context, childMap: Map<Int, List<Int>>) : this(context, mutableListOf<Int>(), childMap)

    init {
        if (groupList.isEmpty()) {
            for ((groupResId) in childMap) {
                groupList.add(groupResId)
            }
        }
    }

    override fun getGroupCount(): Int = groupList.size

    override fun getChildrenCount(groupPosition: Int): Int {
        val group = groupList[groupPosition]
        return childMap[group]?.size ?: 0
    }

    override fun getGroup(groupPosition: Int): Int = groupList[groupPosition]

    override fun getChild(groupPosition: Int, childPosition: Int): Int {
        val group = groupList[groupPosition]
        return childMap[group]?.get(childPosition)!!
    }

    override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()

    override fun getChildId(groupPosition: Int, childPosition: Int): Long = childPosition.toLong()

    override fun hasStableIds(): Boolean = true

    @RequiresApi(Build.VERSION_CODES.M)
    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup): View {
        val groupTextId = getGroup(groupPosition)
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item_group, parent, false)
        val groupTextView = view.findViewById<TextView>(R.id.group_text)
        groupTextView.text = context.getString(groupTextId)

        if (isExpanded) {
            groupTextView.setTextColor(context.getColor(R.color.color_2A2A2C))
        } else {
            groupTextView.setTextColor(context.getColor(R.color.color_888888))
        }

        return view
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup): View {
        val childTextId = getChild(groupPosition, childPosition)
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item_child, parent, false)
        val childTextView = view.findViewById<TextView>(R.id.child_text)
        childTextView.text = context.getString(childTextId)
        return view
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true
}
