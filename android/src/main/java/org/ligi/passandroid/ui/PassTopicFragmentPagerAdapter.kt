package org.ligi.passandroid.ui

import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import org.ligi.passandroid.model.PassClassifier

class PassTopicFragmentPagerAdapter(private val passClassifier: PassClassifier, fragmentManager: FragmentManager) : FragmentStatePagerAdapter(fragmentManager) {

    private lateinit var topic_array: Array<String>

    init {
        notifyDataSetChanged()
    }

    override fun notifyDataSetChanged() {
        val topics = passClassifier.getTopics()
        topic_array = topics.toTypedArray()
        super.notifyDataSetChanged()
    }

    override fun getItem(position: Int) = PassListFragment.newInstance(topic_array[position])

    override fun getCount() = topic_array.size

    override fun getPageTitle(position: Int) = topic_array[position]
}