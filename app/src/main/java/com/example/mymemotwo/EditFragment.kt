package com.example.mymemotwo

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.mymemotwo.data.MEMO_TYPE
import com.example.mymemotwo.databinding.FragmentEditBinding
import com.example.mymemotwo.memodb.MemoEntity
import java.text.SimpleDateFormat

class EditFragment : Fragment() {

    private var _binding: FragmentEditBinding? = null
    private val binding get() = _binding!!

    private val memoViewModel: MemoViewModel by activityViewModels()

    // OnBackPressedCallback(뒤로가기 기능) 객체 선언
    private lateinit var callback: OnBackPressedCallback

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentEditBinding.inflate(inflater, container, false)
        // OnBackPressedCallback (익명 클래스) 객체 생성
        callback = object : OnBackPressedCallback(true) {
            // 뒤로가기 했을 때 실행되는 기능
            override fun handleOnBackPressed() {
                memoTypeActon()
            }
        }
        // 액티비티의 BackPressedDispatcher에 여기서 만든 callback 객체를 등록
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        return binding.root
    }

    @SuppressLint("SimpleDateFormat")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.memoEditText.requestFocus()
        val inputMethodManager =
            context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(binding.memoEditText, InputMethodManager.SHOW_IMPLICIT)

        // EditText(커스텀)에 포커스가 있으면 버튼 표시 없으면 버튼 표시 안함
        binding.memoEditText.setOnFocusChangeListener { _, isFocused ->
            if (isFocused) {
                binding.saveBtn.visibility = View.VISIBLE
                binding.timeBtn.visibility = View.VISIBLE
            } else {
                binding.saveBtn.visibility = View.INVISIBLE
                binding.timeBtn.visibility = View.INVISIBLE
            }
        }

        memoViewModel.MEMOTYPE.observe(viewLifecycleOwner) {
            if (it == MEMO_TYPE.ADD) {
                when (SimpleDateFormat("E").format(System.currentTimeMillis())) {
                    "토" -> {
                        binding.titleTextView.text = highlightString(
                            SimpleDateFormat("yyyy년 MM월 dd일 E요일")
                                .format(System.currentTimeMillis()),
                            "토요일"
                        )
                    }
                    "일" -> {
                        binding.titleTextView.text = highlightString(
                            SimpleDateFormat("yyyy년 MM월 dd일 E요일")
                                .format(System.currentTimeMillis()),
                            "일요일"
                        )
                    }
                    else -> {
                        binding.titleTextView.text = SimpleDateFormat("yyyy년 MM월 dd일 E요일")
                            .format(System.currentTimeMillis())
                    }
                }
            } else {
                when (memoViewModel.selectedMemo.value?.dayOfWeek) {
                    "토" -> {
                        val dataParsing =
                            SimpleDateFormat("yyyyMMddE").parse(memoViewModel.selectedMemo.value!!.timeSource)
                        binding.titleTextView.text = highlightString(
                            SimpleDateFormat("yyyy년 MM월 dd일 E요일").format(dataParsing!!),
                            "토요일"
                        )
                        binding.memoEditText.setText(memoViewModel.selectedMemo.value!!.memo)
                    }
                    "일" -> {
                        val dataParsing =
                            SimpleDateFormat("yyyyMMddE").parse(memoViewModel.selectedMemo.value!!.timeSource)
                        binding.titleTextView.text = highlightString(
                            SimpleDateFormat("yyyy년 MM월 dd일 E요일").format(dataParsing!!),
                            "일요일"
                        )
                        binding.memoEditText.setText(memoViewModel.selectedMemo.value!!.memo)
                    }
                    else -> {
                        val dataParsing =
                            SimpleDateFormat("yyyyMMddE").parse(memoViewModel.selectedMemo.value!!.timeSource)
                        binding.titleTextView.text =
                            SimpleDateFormat("yyyy년 MM월 dd일 E요일").format(dataParsing!!)
                        binding.memoEditText.setText(memoViewModel.selectedMemo.value!!.memo)
                    }
                }
            }
        }

        binding.saveBtn.setOnClickListener {
            when (memoViewModel.MEMOTYPE.value) {
                MEMO_TYPE.ADD, MEMO_TYPE.EMPTY -> {
                    inputMethodManager.hideSoftInputFromWindow(binding.memoEditText.windowToken, 0)
                    binding.memoEditText.clearFocus()
                    memoTypeActon()
                }
                else -> {
                    inputMethodManager.hideSoftInputFromWindow(binding.memoEditText.windowToken, 0)
                    binding.memoEditText.clearFocus()
                    editMemo()
                }
            }
        }

        binding.timeBtn.setOnClickListener {
            val timeData = SimpleDateFormat("a hh:mm").format(System.currentTimeMillis())
            binding.memoEditText.append(timeData)
        }
    }

    // 특정 글자 강조
    private fun highlightString(inputText: String, highlightWord: String): SpannableString {
        val spannableString = SpannableString(inputText)
        val start = inputText.indexOf(highlightWord)
        val end = start + highlightWord.length
        spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#C53C3C")),
            start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannableString
    }

    // 메모타입(수정, 새로만들기)에 따른 액션 기능
    fun memoTypeActon() {
        when (memoViewModel.MEMOTYPE.value) {
            MEMO_TYPE.ADD -> {
                addMemo()
                findNavController().navigate(R.id.action_editFragment_to_listFragment)
            }
            else -> {
                editMemo()
                findNavController().navigate(R.id.action_editFragment_to_listFragment)
            }
        }
    }


    // 메모 추가
    @SuppressLint("SimpleDateFormat")
    fun addMemo() {
        val currentDateSource = System.currentTimeMillis()
        val timeSource = SimpleDateFormat("yyyyMMddE").format(currentDateSource)
        val day = SimpleDateFormat("dd").format(currentDateSource)
        val dayOfWeek = SimpleDateFormat("E").format(currentDateSource)
        val month = SimpleDateFormat("MM").format(currentDateSource)
        val year = SimpleDateFormat("yyyy").format(currentDateSource)

        val memo = MemoEntity(
            null, binding.memoEditText.text.toString(), timeSource, day, dayOfWeek, month, year
        )

        if (memo.memo.isNotBlank()) {
            memoViewModel.addMemo(memo)
        }
    }


    @SuppressLint("SimpleDateFormat")
    fun editMemo() {
        val memo = MemoEntity(
            memoViewModel.selectedMemo.value!!.id,
            binding.memoEditText.text.toString(),
            memoViewModel.selectedMemo.value!!.timeSource,
            memoViewModel.selectedMemo.value!!.day,
            memoViewModel.selectedMemo.value!!.dayOfWeek,
            memoViewModel.selectedMemo.value!!.month,
            memoViewModel.selectedMemo.value!!.year
        )

        val currentDateSource = System.currentTimeMillis()
        val timeSource = SimpleDateFormat("yyyyMMddE").format(currentDateSource)

        if (memoViewModel.selectedMemo.value!!.timeSource == timeSource
            && binding.memoEditText.text.toString().isEmpty()) {
            memoViewModel.deleteMemo(memoViewModel.selectedMemo.value!!)
        } else {
            memoViewModel.editMemo(memo)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        callback.remove()
    }
}