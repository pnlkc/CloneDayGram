package com.example.mymemotwo

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mymemotwo.data.LIST_TYPE
import com.example.mymemotwo.data.MEMO_TYPE
import com.example.mymemotwo.data.PreferenceKey.Companion.KEY_MEMO_TYPE
import com.example.mymemotwo.data.PreferenceKey.Companion.KYE_PREFS
import com.example.mymemotwo.databinding.FragmentListBinding
import com.example.mymemotwo.memodb.MemoEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ListFragment : Fragment(), MemoTouchInterface {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private val memoViewModel: MemoViewModel by activityViewModels()

    private lateinit var adapter: MemoAdapter

    // OnBackPressedCallback (뒤로가기 기능) 객체 선언
    private lateinit var callback: OnBackPressedCallback

    private var matchMemoList: MutableList<MemoEntity> = mutableListOf()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)

        // OnBackPressedCallback (익명 클래스) 객체 생성
        callback = object : OnBackPressedCallback(true) {
            // 뒤로가기 했을 때 실행되는 기능
            var backWait: Long = 0
            override fun handleOnBackPressed() {
                if (binding.monthScrollView.visibility == View.VISIBLE) {
                    binding.monthScrollView.visibility = View.GONE
                } else if (binding.yearScrollView.visibility == View.VISIBLE) {
                    binding.yearScrollView.visibility = View.GONE
                }
                else {
                    if (System.currentTimeMillis() - backWait >= 2000) {
                        backWait = System.currentTimeMillis()
                        Toast.makeText(context, "뒤로가기 버튼을 한번 더 누르면 종료됩니다", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        activity?.finish()
                    }
                }

            }
        }
        // 액티비티의 onBackPressedDispatcher에 여기서 만든 callback 객체를 등록
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        return binding.root
    }

    @SuppressLint("SimpleDateFormat")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = MemoAdapter(this, loadMemoListType())
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)


        // 리스트 버튼을 누르면 실행되는 코드
        binding.listChangeBtn.setOnClickListener {
            if (binding.listChangeBtn.isChecked) {
                adapter = MemoAdapter(this, LIST_TYPE.BOX)
                binding.recyclerView.adapter = adapter
                setYearAndMonth()
            } else {
                adapter = MemoAdapter(this, LIST_TYPE.SIMPLE)
                binding.recyclerView.adapter = adapter
                setYearAndMonth()
            }
            saveMemoListType()
        }

        // 앱 실행시 현재 연도와 달이 기본 설정되도록 하는 코드
        binding.yearTextView.text = memoViewModel.yearAndMonth.year
        binding.monthTextView.text = memoViewModel.yearAndMonth.month

        CoroutineScope(Dispatchers.IO).launch {
            // 초기 데이터 설정이 2020.01.01 이면 1500 딜레이 주면 됨
            delay(1500)
            autoAddMemoUntilYesterday()
        }

        // 메모가 추가, 변경되어 readAllData 변경시 호출
        memoViewModel.readAllData.observe(viewLifecycleOwner) {
            setYearAndMonth()
        }

        // 메모 추가 버튼
        binding.addBtn.setOnClickListener {
            val currentTimeInfo = SimpleDateFormat("yyyyMMddE").format(System.currentTimeMillis())
            for (i in 0 until matchMemoList.size) {
                if (matchMemoList[i].timeSource == currentTimeInfo) {
                    memoViewModel.selectedMemo.value = matchMemoList[i]
                    memoViewModel.MEMOTYPE.value = MEMO_TYPE.EDIT
                    findNavController().navigate(R.id.action_listFragment_to_editFragment)
                    return@setOnClickListener
                } else {
                    memoViewModel.MEMOTYPE.value = MEMO_TYPE.ADD
                }
            }
            findNavController().navigate(R.id.action_listFragment_to_editFragment)
        }

        // 월 바꾸기 버튼
        binding.monthTextView.setOnClickListener {
            binding.monthScrollView.visibility = View.VISIBLE

            val currentMonth = SimpleDateFormat("MM").format(System.currentTimeMillis())

            val monthTextViewList =
                listOf(binding.janBtn, binding.febBtn, binding.marBtn, binding.aprBtn,
                    binding.mayBtn, binding.junBtn, binding.julBtn, binding.augBtn, binding.sepBtn,
                    binding.octBtn, binding.novBtn, binding.decBtn)

            for (i in 0 until currentMonth.toInt()) {
                if (monthTextViewList[i].text.toString() == memoViewModel.yearAndMonth.month) {
                    monthTextViewList[i].setBackgroundResource(R.drawable.btn_bg)
                } else {
                    monthTextViewList[i].setBackgroundResource(R.drawable.btn_bg_unselected)
                }
                clickMonth(monthTextViewList[i])
            }
            for (j in currentMonth.toInt() until monthTextViewList.size) {
                monthTextViewList[j].setBackgroundResource(R.drawable.btn_bg_disable)
            }
        }


        // 연도 바꾸기 버튼
        binding.yearTextView.setOnClickListener {
            binding.yearScrollView.visibility = View.VISIBLE

            val yearTextViewList =
                listOf(binding.twentyTwentyBtn,
                    binding.twentyTwentyOneBtn,
                    binding.twentyTwentyTwoBtn)

            yearTextViewList.forEach {
                if (it.text.toString() == memoViewModel.yearAndMonth.year) {
                    it.setTextColor(resources.getColor(R.color.text, null))
                } else {
                    it.setTextColor(resources.getColor(R.color.text_unselected, null))
                }
                clickYear(it)
            }
        }

    }

    // 메모 데이터에서 설정에 해당하는 연도, 달 데이터만 추출
    private fun setYearAndMonth() {
        matchMemoList.clear()
        memoViewModel.readAllData.value!!.forEach { memo ->
            if (memo.month == memoViewModel.yearAndMonth.month
                && memo.year == memoViewModel.yearAndMonth.year
            ) {
                matchMemoList.add(memo)
            }
        }
        adapter.setMemoList(matchMemoList)
    }

    // 어제까지 메모 자동 추가하는 함수
    @SuppressLint("SimpleDateFormat")
    fun autoAddMemoUntilYesterday() {
        val dateFormat = SimpleDateFormat("yyyyMMddE")
        val today = dateFormat.format(Date())
        val calendar = Calendar.getInstance()

        val lastMemo = memoViewModel.readAllData.value!!.last().timeSource

        val todayParse = dateFormat.parse(today)
        val todayCompareDate = SimpleDateFormat("yyyyMMdd").format(todayParse!!)
        val lastMemoParse = dateFormat.parse(lastMemo)
        val lastMemoCompareDate = SimpleDateFormat("yyyyMMdd").format(lastMemoParse!!)

        calendar.time = lastMemoParse
        calendar.add(Calendar.DATE, 1)
        var timeSource = dateFormat.format(calendar.time)

        if (lastMemoCompareDate < todayCompareDate) {
            while (timeSource != today) {
                val memo = MemoEntity(
                    null,
                    "",
                    timeSource,
                    SimpleDateFormat("dd").format(calendar.time),
                    SimpleDateFormat("E").format(calendar.time),
                    SimpleDateFormat("MM").format(calendar.time),
                    SimpleDateFormat("yyyy").format(calendar.time)
                )
                memoViewModel.addMemo(memo)

                calendar.add(Calendar.DATE, 1)
                timeSource = dateFormat.format(calendar.time)
            }
        }
    }


    // 월(Month) 클릭 이벤트 처리
    private fun clickMonth(view: TextView) {
        view.setOnClickListener {
            binding.monthTextView.text = view.text.toString()
            memoViewModel.yearAndMonth.month = view.text.toString()
            setYearAndMonth()
            binding.monthScrollView.visibility = View.GONE
        }
    }

    // 연도(Year) 클릭 이벤트 처리
    private fun clickYear(view: TextView) {
        view.setOnClickListener {
            binding.yearTextView.text = view.text.toString()
            memoViewModel.yearAndMonth.year = view.text.toString()
            setYearAndMonth()
            binding.yearScrollView.visibility = View.GONE
        }
    }

    // 메모를 눌렀을 때
    override fun memoClicked(position: Int) {
        memoViewModel.selectedMemo.value = matchMemoList[position]
        if (memoViewModel.selectedMemo.value!!.memo == "") {
            memoViewModel.MEMOTYPE.value = MEMO_TYPE.EMPTY
        } else {
            memoViewModel.MEMOTYPE.value = MEMO_TYPE.EDIT
        }
        findNavController().navigate(R.id.action_listFragment_to_editFragment)
    }

    // 메모를 길게 눌렀을 때
    override fun memoLongClicked(position: Int) {

        // 삭제 다이얼로그 보여주기
        AlertDialog.Builder(context)
            .setTitle("MyMemoTwo")
            .setMessage(matchMemoList[position].year +
                    ".${matchMemoList[position].month}" +
                    ".${matchMemoList[position].day}" +
                    " 메모를 삭제하시겠습니까?")
            .setPositiveButton("확인") { _, _ ->
                val memo = MemoEntity(
                    matchMemoList[position].id,
                    "",
                    matchMemoList[position].timeSource,
                    matchMemoList[position].day,
                    matchMemoList[position].dayOfWeek,
                    matchMemoList[position].month,
                    matchMemoList[position].year
                )
                memoViewModel.editMemo(memo)
                Log.d("로그", "setPositiveButton 호출됨")
            }
            .setNegativeButton("취소"
            ) { _, _ -> Log.d("로그", "setNegativeButton 호출됨") }
            .create()
            .show()
    }

    // 메모 리스트 타입 저장
    private fun saveMemoListType() {
        val sharedPreferences = requireActivity()
            .getSharedPreferences(KYE_PREFS, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putBoolean(KEY_MEMO_TYPE, binding.listChangeBtn.isChecked)
        editor.apply()
    }

    // 메모 리스트 타입 불러오기
    private fun loadMemoListType(): LIST_TYPE {
        val sharedPreferences = requireActivity()
            .getSharedPreferences(KYE_PREFS, Context.MODE_PRIVATE)
        val result = sharedPreferences.getBoolean(KEY_MEMO_TYPE, true)
        return if (result) {
            binding.listChangeBtn.isChecked = true
            LIST_TYPE.BOX
        } else {
            binding.listChangeBtn.isChecked = false
            LIST_TYPE.SIMPLE
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        callback.remove()
    }
}