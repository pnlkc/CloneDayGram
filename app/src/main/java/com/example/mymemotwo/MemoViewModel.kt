package com.example.mymemotwo

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.mymemotwo.data.LIST_TYPE
import com.example.mymemotwo.data.MEMO_TYPE
import com.example.mymemotwo.memodb.MemoDatabase
import com.example.mymemotwo.memodb.MemoEntity
import com.example.mymemotwo.memodb.MemoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

data class YearAndMonth(
    var year: String,
    var month: String
)

@SuppressLint("SimpleDateFormat")
class MemoViewModel(application: Application) : AndroidViewModel(application) {

    private val _readAllData: LiveData<List<MemoEntity>>
    val readAllData: LiveData<List<MemoEntity>>
        get() = _readAllData
    val repository: MemoRepository

    private val _selectedMemo = MutableLiveData<MemoEntity>(MemoEntity(null, ""))
    val selectedMemo: MutableLiveData<MemoEntity> = _selectedMemo

    private val _MEMOTYPE = MutableLiveData<MEMO_TYPE>(MEMO_TYPE.ADD)
    val MEMOTYPE: MutableLiveData<MEMO_TYPE>
        get() = _MEMOTYPE

    var yearAndMonth: YearAndMonth

    init {
        val memoDAO = MemoDatabase.getInstance(application)!!.memoDAO()
        repository = MemoRepository(memoDAO)
        _readAllData = repository.readAllData
        yearAndMonth = YearAndMonth(
            SimpleDateFormat("yyyy").format(System.currentTimeMillis()),
            SimpleDateFormat("MM").format(System.currentTimeMillis())
        )
    }

    // 코루틴을 통해 Repository에 있는 addMemo() 메서드 실행
    fun addMemo(memo: MemoEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addMemo(memo)
        }
    }

    // 코루틴을 통해 Repository에 있는 deleteMemo() 메서드 실행
    fun deleteMemo(memo: MemoEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteMemo(memo)
        }
    }

    fun editMemo(memo: MemoEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.editMemo(memo)
        }
    }
}