package com.example.mymemotwo.memodb

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

// entities는 사용할 Entitiy를 선언 해주면 됨 []는 배열을 의미
// version은 Entity 구조 변경시 구분해주는 역할
@Database(entities = [MemoEntity::class], version = 1)
abstract class MemoDatabase : RoomDatabase() {

    abstract fun memoDAO(): MemoDAO

    // 싱글톤으로 데이터 베이스 만들기
    companion object {
        @Volatile
        var INSTANCE: MemoDatabase? = null

        fun getInstance(context: Context): MemoDatabase? {
            if (INSTANCE == null) {
                synchronized(MemoDatabase::class) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        MemoDatabase::class.java,
                        "memo.db"
                    ).addCallback(object : Callback() {
                        // 데이터베이스 생성시 최초 데이터 등록
                        @SuppressLint("SimpleDateFormat")
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Log.d("로그", "MemoDatabase - onCreate() 호출됨")
                            CoroutineScope(Dispatchers.IO).launch {
                                Log.d("로그", "MemoDatabase - CoroutineScope 시작 호출됨")
                                initData(context).forEach {
                                    getInstance(context)!!.memoDAO().add(it)
                                }
                                Log.d("로그", "MemoDatabase - CoroutineScope 끝 호출됨")
                            }
                        }

                        // 초기 데이터 추가
                        @SuppressLint("SimpleDateFormat")
                        suspend fun initData(context: Context): MutableList<MemoEntity> {
                            Log.d("로그", "MemoDatabase - initData() 시작 호출됨")
                            val memoList = mutableListOf<MemoEntity>()

                            return withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                                val dateFormat = SimpleDateFormat("yyyyMMddE")
                                val today = dateFormat.format(Date())
                                val calendar = Calendar.getInstance()

                                // month는 0부터 11까지
                                calendar.set(2020, 0, 1)

                                var timeSource = dateFormat.format(calendar.time)

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

                                    memoList.add(memo)

                                    calendar.add(Calendar.DATE, 1)
                                    timeSource = dateFormat.format(calendar.time)
                                }
                                Log.d("로그", "MemoDatabase - initData() 끝 호출됨")
                                memoList
                            }
                        }
                    }).build()
                }
            }
            return INSTANCE
        }
    }
}


