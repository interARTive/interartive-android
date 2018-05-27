package com.github.zakaprov.interartive.domain

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.zakaprov.interartive.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_detail.*

class DetailFragment : Fragment() {

    companion object {
        private val ARGUMENT_IMAGE_NAME = "DetailFragment_MarkerDetails"

        fun newInstance(imageName: String?): DetailFragment {
            val args = Bundle()
            args.putString(ARGUMENT_IMAGE_NAME, imageName)

            val fragment = DetailFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private val imageName by lazy { arguments?.getString(ARGUMENT_IMAGE_NAME) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Picasso.get()
            .load("file:///android_asset/images/$imageName")
            .into(detail_image_top)

        detail_button_back.setOnClickListener { activity?.onBackPressed() }
    }
}