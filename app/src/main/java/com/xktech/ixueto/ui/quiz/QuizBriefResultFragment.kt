package com.xktech.ixueto.ui.quiz

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.fragment.findNavController
import com.xktech.ixueto.R
import com.xktech.ixueto.databinding.FragmentQuizBriefResultBinding
import com.xktech.ixueto.model.QuizStateEnum
import com.xktech.ixueto.utils.EnumUtils

class QuizBriefResultFragment : Fragment() {
    private var rootView: View? = null
    private var binding: FragmentQuizBriefResultBinding? = null
    private var professionId: Int? = null
    private var subjectId: Int? = null
    private var courseId: Int? = null
    private var courseName: String? = null
    private var result: Short? = null
    private lateinit var successView: ViewGroup
    private lateinit var backStudyButton: Button
    private lateinit var failView: ViewGroup
    private lateinit var resetQuizButton: Button
    private lateinit var resetView: ViewGroup
    private lateinit var resetStudyButton: Button
    private lateinit var preSavedStateHandle: SavedStateHandle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            initView()
        }
        return rootView
    }

    private fun initView() {
        binding = FragmentQuizBriefResultBinding.inflate(layoutInflater)
        rootView = binding!!.root
        successView = binding!!.success
        backStudyButton = binding!!.backStudy
        failView = binding!!.fail
        resetQuizButton = binding!!.resetQuiz
        resetView = binding!!.reset
        resetStudyButton = binding!!.resetStudy
        professionId = arguments?.getInt("professionId")
        subjectId = arguments?.getInt("subjectId")
        courseId = arguments?.getInt("courseId")
        courseName = arguments?.getString("courseName")
        result = arguments?.getShort("result")
        preSavedStateHandle = findNavController().previousBackStackEntry!!.savedStateHandle
        result?.let {
            when (EnumUtils.getQuizStateEnum(it)) {
                QuizStateEnum.NOT_PASS -> {
                    preSavedStateHandle["quiz"] = QuizStateEnum.NOT_PASS
                    playViewAnimation(failView)
                }
                QuizStateEnum.PASS -> {
                    preSavedStateHandle["quiz"] = QuizStateEnum.PASS
                    playViewAnimation(successView)
                }
                QuizStateEnum.STUDY_RESET -> {
                    preSavedStateHandle["quiz"] = QuizStateEnum.STUDY_RESET
                    playViewAnimation(resetView)
                    var notification = NotificationCompat.Builder(requireContext(), "resetStudy")
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("行知学徒网：学习重置通知")
                        .setStyle(
                            NotificationCompat.BigTextStyle()
                                .bigText("很遗憾，由于您多次考试未达标准，您在《${courseName}》的学习已经被重置，请重新学习！")
                        )
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                    with(NotificationManagerCompat.from(requireContext())) {
                        notify(1, notification.build())
                    }
                }
                else -> {
                    failView.visibility = View.VISIBLE
                }
            }
        }
        backStudyButton.setOnClickListener {
            findNavController().popBackStack()
        }
        resetQuizButton.setOnClickListener {
            findNavController().popBackStack()
        }
        resetStudyButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun playViewAnimation(viewGroup: ViewGroup) {
        val targetViewGroup = viewGroup.getChildAt(1) as ViewGroup
        val titleTextView = targetViewGroup.getChildAt(0)
        val operationButton = targetViewGroup.getChildAt(1)
        titleTextView.alpha = 0f
        operationButton.alpha = 0f
        viewGroup.visibility = View.VISIBLE
        val scaleSmallToBigX = PropertyValuesHolder.ofFloat(View.SCALE_X, 0.5f, 1.5f)
        val scaleSmallToBigY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.5f, 1.5f)
        val alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f)
        val titleAnimator =
            ObjectAnimator.ofPropertyValuesHolder(
                titleTextView,
                scaleSmallToBigX,
                scaleSmallToBigY,
                alpha
            ).apply {
                interpolator = DecelerateInterpolator()
                duration = 200
            }
        val operationButtonAnimator =
            ObjectAnimator.ofFloat(operationButton, View.ALPHA, 0f, 1f).apply {
                duration = 200
            }
        val animatorSet = AnimatorSet()
        val (anim1X, anim1Y) = titleTextView.let { view ->
            SpringAnimation(view, DynamicAnimation.SCALE_X) to SpringAnimation(
                view,
                DynamicAnimation.SCALE_Y,
            )
        }
        anim1X.addUpdateListener { _, value, _ ->
            anim1Y.animateToFinalPosition(value)
        }
        val iconForce = SpringForce().apply {
            dampingRatio = SpringForce.DAMPING_RATIO_HIGH_BOUNCY
            stiffness = SpringForce.STIFFNESS_MEDIUM
        }
        anim1X.spring = iconForce
        animatorSet.play(titleAnimator)
        animatorSet.start()
        titleAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                TODO("Not yet implemented")
            }

            override fun onAnimationEnd(animation: Animator) {
                anim1X.animateToFinalPosition(1f)
                anim1X.addEndListener { _, _, _, _ ->
                    operationButtonAnimator.start()
                }
            }

            override fun onAnimationCancel(animation: Animator) {
                TODO("Not yet implemented")
            }

            override fun onAnimationRepeat(animation: Animator) {
                TODO("Not yet implemented")
            }
        })
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            QuizBriefResultFragment()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}