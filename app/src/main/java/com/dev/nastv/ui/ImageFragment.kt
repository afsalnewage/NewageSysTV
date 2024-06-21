package com.dev.nastv.ui

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.SpannedString
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.text.style.SubscriptSpan
import android.text.style.SuperscriptSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.italic
import androidx.core.text.underline
import com.dev.nastv.R
import com.dev.nastv.databinding.FragmentImageBinding
import com.dev.nastv.databinding.FragmentVideoBinding
import com.dev.nastv.model.TvMedia
import com.dev.nastv.model.Type
import com.dev.nastv.model.getType
import com.dev.nastv.uttils.AppUittils
import com.dev.nastv.uttils.AppUittils.applyFadeInAnimation
import com.dev.nastv.uttils.AppUittils.applySlideInAnimation
import com.dev.nastv.uttils.AppUittils.formatDateString
import com.dev.nastv.uttils.AppUittils.getOrdinalSuffix
import com.dev.nastv.uttils.AppUittils.loadImage
import com.dev.nastv.uttils.CustomTypefaceSpan
import com.smb.app.addsapp.model.MediaItemData
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.Date
import java.util.concurrent.TimeUnit


private const val ARG_MEDIA_IMG = "media_data_img"

class ImageFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null
    private var _binding: FragmentImageBinding? = null

    private var newjoineframviews = listOf<View>()
    private val binding get() = _binding!!
    private var mediaData: TvMedia? = null
    private lateinit var mediaTyp: Type
    val party = Party(
        speed = 0f,
        maxSpeed = 30f,
        damping = 0.9f,
        spread = 360,
        colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
        emitter = Emitter(duration = 10000, TimeUnit.MILLISECONDS).max(1000),
        position = Position.Relative(0.5, 0.2)
    )

    val anniversary_party = Party(
        speed = 0f,
        maxSpeed = 30f,
        damping = 0.9f,
        spread = 360,
        colors = listOf(0xfce23a, 0xff706d, 0xf7906d, 0xb044def),
        emitter = Emitter(duration = 10000, TimeUnit.MILLISECONDS).max(1000),
        position = Position.Relative(0.6, 0.2)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

            mediaData = it.getSerializable(ARG_MEDIA_IMG) as TvMedia

            mediaTyp = getType(mediaData!!.event_type)
        }




    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentImageBinding.inflate(inflater, container, false)



        return binding.root
    }

    companion object {

        @JvmStatic
        fun newInstance(media: TvMedia) =
            ImageFragment().apply {
                arguments = Bundle().apply {

                    putSerializable(ARG_MEDIA_IMG, media)
                }
            }
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        when (mediaTyp) {
            Type.Image -> {
                binding.customFrame.visibility=View.VISIBLE
                loadImage(mediaData?.file_url, binding.customImage)
            }
            Type.Birthday -> {
                binding.newjoineeFrame.visibility = View.GONE
                binding.anniversaryFrame.visibility = View.GONE
                binding.customFrame.visibility=View.GONE
                binding.birthdayFrame.visibility = View.VISIBLE
                binding.birthdayPersonName.text = mediaData?.user_name
                binding.popperView.start(party = party)
                binding.birthdayDat.text = formatDateString(mediaData!!.end_date)
                loadImage(mediaData?.file_url, binding.imageBg)

            }

            Type.Anniversary -> {
                binding.newjoineeFrame.visibility = View.GONE
                binding.anniversaryFrame.visibility = View.VISIBLE
                binding.birthdayFrame.visibility = View.GONE
                binding.customFrame.visibility=View.GONE
                loadImage(mediaData?.file_url, binding.profileAnniversary)
                binding.popperView.start(party = anniversary_party)
                binding.anniversaryProfileName.text = mediaData?.user_name
                binding.anniversaryProfileDesignation.text = mediaData?.user_position
                binding.anniversaryTittle.text =
                    setAnniversaryYearText(mediaData?.anniversary_year ?: 0)


            }

            Type.NewJoinee -> {
                binding.newjoineeFrame.visibility = View.VISIBLE
                binding.anniversaryFrame.visibility = View.GONE
                binding.birthdayFrame.visibility = View.GONE
                binding.customFrame.visibility=View.GONE

                binding.apply {
                    loadImage(mediaData?.file_url, this.profileNewjoinee)
                    this.newjoineeProfileName.text = mediaData?.user_name
                    this.newjoineeDesignation.text = mediaData?.user_position
                    this.textJoiningDate.text =
                        "Date of joining: ${formatDateString(mediaData!!.event_date)}"
                    this.textEducation.text = mediaData?.educational_qualification
                    this.textProfessionalBackground.text = mediaData?.professional_background
                    this.textHobbies.text = mediaData?.hobbies
                }

                applySlideInAnimation(binding.logo1)
                applySlideInAnimation(binding.newjoineeProfileName)
                applySlideInAnimation(binding.newjoineeMessage)
                applySlideInAnimation(binding.newjoineeProfileName)
                applySlideInAnimation(binding.newjoineeDesignation)
                applySlideInAnimation(binding.education)
                applySlideInAnimation(binding.textEducation)
                applySlideInAnimation(binding.hobbies)
                applySlideInAnimation(binding.textHobbies)
                applySlideInAnimation(binding.professional)
                applySlideInAnimation(binding.textProfessionalBackground)
            }

            else -> {}
        }


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setAnniversaryYearText(year: Int): SpannedString {
        val suffix = getOrdinalSuffix(year)
        val regularBrush = requireContext().resources.getFont(R.font.regular_brash)
        val customFont = requireContext().resources.getFont(R.font.sansita_regular)
        val buildedText = buildSpannedString {

            append("Happy ")

            setSpan(
                CustomTypefaceSpan("", regularBrush),
                length - "Happy ".length,
                length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            append("$year $suffix")
            setSpan(SuperscriptSpan(), length - 2, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(RelativeSizeSpan(0.5f), length - 2, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(
                CustomTypefaceSpan("", regularBrush),
                length - 2,
                length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )


            append("\nWork Anniversary ")

            setSpan(
                CustomTypefaceSpan("", regularBrush),
                length - "Work Anniversary ".length,
                length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )


        }
        return buildedText
    }

    override fun onPause() {
        super.onPause()

        binding.apply {
            this.logo1.visibility = View.INVISIBLE
            this.newjoineeProfileName.visibility = View.INVISIBLE
            this.newjoineeMessage.visibility = View.INVISIBLE
            this.newjoineeProfileName.visibility = View.INVISIBLE
            this.newjoineeDesignation.visibility = View.INVISIBLE
            this.textJoiningDate.visibility = View.INVISIBLE
            this.education.visibility = View.INVISIBLE
            this.textJoiningDate.visibility = View.INVISIBLE
            this.textEducation.visibility = View.INVISIBLE
            this.textEducation.visibility = View.INVISIBLE
            this.textHobbies.visibility = View.INVISIBLE
            this.hobbies.visibility = View.INVISIBLE
            this.professional.visibility = View.INVISIBLE
            this.textProfessionalBackground.visibility = View.INVISIBLE
            this.profileNewjoinee.visibility = View.INVISIBLE
            this.imgBannerNewjoinee.visibility = View.INVISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
//        binding.newjoineeFrame.visibility=View.VISIBLE
//        binding.anniversaryFrame.visibility=View.GONE
//        binding.birthdayFrame.visibility=View.GONE

        when (mediaTyp) {
            Type.NewJoinee -> {
                applyFadeInAnimation(binding.imgBannerNewjoinee)
                applyFadeInAnimation(binding.profileNewjoinee)
                applySlideInAnimation(binding.logo1)
                applySlideInAnimation(binding.newjoineeProfileName)
                applySlideInAnimation(binding.newjoineeMessage)
                applySlideInAnimation(binding.newjoineeProfileName)
                applySlideInAnimation(binding.newjoineeDesignation)
                applySlideInAnimation(binding.education)
                applySlideInAnimation(binding.textJoiningDate)
                applySlideInAnimation(binding.textEducation)
                applySlideInAnimation(binding.hobbies)
                applySlideInAnimation(binding.textHobbies)
                applySlideInAnimation(binding.professional)
                applySlideInAnimation(binding.textProfessionalBackground)
            }

            Type.Birthday -> {
                binding.popperView.start(party = party)
            }

            Type.Anniversary -> {
                binding.popperView.start(party = anniversary_party)
            }

            else -> {

            }
        }

    }
}