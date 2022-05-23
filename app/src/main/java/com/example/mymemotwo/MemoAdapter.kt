package com.example.mymemotwo

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.mymemotwo.data.LIST_TYPE
import com.example.mymemotwo.databinding.MemoItemBoxBinding
import com.example.mymemotwo.databinding.MemoItemSimpleBinding
import com.example.mymemotwo.memodb.MemoEntity

class MemoAdapter(private var memoTouchInterface: MemoTouchInterface, private val listType: LIST_TYPE) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var memoList = listOf<MemoEntity>()
    lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context
        return when (listType) {
            LIST_TYPE.BOX -> {
                val binding =
                    MemoItemBoxBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                BoxMemoViewHolder(binding)
            }
            else -> {
                val binding = MemoItemSimpleBinding.inflate(LayoutInflater.from(parent.context),
                    parent,
                    false)
                SimpleMemoViewHolder(binding)
            }
        }


    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (listType) {
            LIST_TYPE.BOX -> {
                val binding = (holder as BoxMemoViewHolder).binding

                if (memoList[position].memo.isEmpty()) {
                    binding.linearLayout.visibility = View.GONE
                    binding.noMemoImageView.visibility = View.VISIBLE

                    when (memoList[position].dayOfWeek) {
                        "토", "일" -> binding.noMemoImageView.setImageResource(R.drawable.no_memo_weekend)
                        else -> binding.noMemoImageView.setImageResource(R.drawable.no_memo)
                    }

                    binding.frameLayout.setOnClickListener {
                        memoTouchInterface.memoClicked(position)
                    }
                } else {
                    binding.linearLayout.visibility = View.VISIBLE
                    binding.noMemoImageView.visibility = View.INVISIBLE

                    when (memoList[position].dayOfWeek) {
                        "토", "일" -> binding.dayTextView.setTextColor(
                            ContextCompat.getColor(context, R.color.highlight_text))
                        else -> binding.dayTextView.setTextColor(
                            ContextCompat.getColor(context, R.color.text))
                    }

                    binding.dayTextView.text = memoList[position].day
                    binding.dayOfWeekTextView.text = memoList[position].dayOfWeek
                    binding.memoEditText.text = memoList[position].memo

                    binding.frameLayout.setOnClickListener {
                        memoTouchInterface.memoClicked(position)
                    }

                    binding.frameLayout.setOnLongClickListener {
                        memoTouchInterface.memoLongClicked(position)
                        return@setOnLongClickListener true
                    }
                }
            }
            else -> {
                val binding = (holder as SimpleMemoViewHolder).binding

                if (memoList[position].memo.isEmpty()) {
                    binding.simpleTextView.visibility = View.GONE
                } else {
                    binding.simpleTextView.visibility = View.VISIBLE
                    when (memoList[position].dayOfWeek) {
                        "토", "일" -> {
                            val text =
                                "${memoList[position].day} ${memoList[position].dayOfWeek}요일   /  ${memoList[position].memo}"

                            binding.simpleTextView.text = highlightStringTwo(
                                text,
                                memoList[position].day,
                                "${memoList[position].dayOfWeek}요일")
                        }
                        else -> {
                            val text =
                                "${memoList[position].day} ${memoList[position].dayOfWeek}요일   /  ${memoList[position].memo}"

                            binding.simpleTextView.text = highlightString(
                                text,
                                memoList[position].day,
                                "${memoList[position].dayOfWeek}요일")
                        }
                    }

                    binding.linearLayout.setOnClickListener {
                        memoTouchInterface.memoClicked(position)
                    }

                    binding.linearLayout.setOnLongClickListener {
                        memoTouchInterface.memoLongClicked(position)
                        return@setOnLongClickListener true
                    }
                }
            }
        }


    }

    override fun getItemCount(): Int {
        return memoList.size
    }


    //    StyleSpan(Typeface.BOLD)
    //    ForegroundColorSpan(Color.parseColor("#C53C3C"))
    // 특정 단어 두개 볼드
    private fun highlightString(
        inputText: String,
        firstWord: String,
        secondWord: String,
    ): SpannableString {
        val spannableString = SpannableString(inputText)
        val firstStart = inputText.indexOf(firstWord)
        val firstEnd = firstStart + firstWord.length
        spannableString.setSpan(StyleSpan(Typeface.BOLD),
            firstStart, firstEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        val secondStart = inputText.indexOf(secondWord)
        val secondEnd = secondStart + secondWord.length
        spannableString.setSpan(StyleSpan(Typeface.BOLD),
            secondStart, secondEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannableString
    }

    // 특정 단어 두개 볼드 밑 두번째 단어 색상 변경
    private fun highlightStringTwo(
        inputText: String,
        firstWord: String,
        secondWord: String,
    ): SpannableString {
        val spannableString = SpannableString(inputText)
        val firstStart = inputText.indexOf(firstWord)
        val firstEnd = firstStart + firstWord.length
        spannableString.setSpan(StyleSpan(Typeface.BOLD),
            firstStart, firstEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        val secondStart = inputText.indexOf(secondWord)
        val secondEnd = secondStart + secondWord.length
        spannableString.setSpan(StyleSpan(Typeface.BOLD),
            secondStart, secondEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#C53C3C")),
            secondStart, secondEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannableString
    }


    // 리사이클러뷰에 표시할 목록 설정하는 함수
    @SuppressLint("NotifyDataSetChanged")
    fun setMemoList(memo: List<MemoEntity>) {
        memoList = memo

        // 리사이클러뷰의 데이터가 변경되었다고 알려주는 코드
        notifyDataSetChanged()
    }
}

class BoxMemoViewHolder(val binding: MemoItemBoxBinding) : RecyclerView.ViewHolder(binding.root)

class SimpleMemoViewHolder(val binding: MemoItemSimpleBinding) :
    RecyclerView.ViewHolder(binding.root)