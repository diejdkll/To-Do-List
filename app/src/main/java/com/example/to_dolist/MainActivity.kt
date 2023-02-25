package com.example.to_dolist

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.example.to_dolist.databinding.ActivityMainBinding
import com.example.to_dolist.databinding.TodoContentBinding
import com.example.to_dolist.databinding.TodoDateBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.write.setOnClickListener {
            startActivity(Intent(this@MainActivity, ToDoWriteActivity::class.java))
        }

        getToDoList()

        binding.searchEdittext.doAfterTextChanged {
            searchToDoList(it.toString())
        }
    }

    override fun onResume() {
        super.onResume()

        getToDoList()
    }

    // 화면 클릭시 키보드 숨기기 및 포커스 제거
    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action === MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm: InputMethodManager =
                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    fun makeToDoList(todoList: ArrayList<ToDo>){
        binding.todoList.adapter = ToDoListRecyclerAdapter(
            todoList,
            LayoutInflater.from(this@MainActivity),
            this@MainActivity
        )
    }

    fun getToDoList() {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://mellowcode.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val retrofitService = retrofit.create(RetrofitService::class.java)

        val header = HashMap<String, String>()
        val sp = this.getSharedPreferences(
            "user_info",
            Context.MODE_PRIVATE
        )
        val token = sp.getString("token", "")
        header.put("Authorization", "token " + token!!)

        retrofitService.getList(header).enqueue(object : Callback<ArrayList<ToDo>> {
            override fun onResponse(
                call: Call<ArrayList<ToDo>>,
                response: Response<ArrayList<ToDo>>
            ) {
                if (response.isSuccessful) {
                    val todoList = response.body()

                    makeToDoList(todoList!!)
                }
            }

            override fun onFailure(call: Call<ArrayList<ToDo>>, t: Throwable) {

            }
        })
    }

    fun changeToDoComplete(todoId: Int){
        val retrofit = Retrofit.Builder()
            .baseUrl("http://mellowcode.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val retrofitService = retrofit.create(RetrofitService::class.java)

        val header = HashMap<String, String>()
        val sp = this.getSharedPreferences(
            "user_info",
            Context.MODE_PRIVATE
        )
        val token = sp.getString("token", "")
        header.put("Authorization", "token " + token!!)

        retrofitService.changeComplete(header, todoId).enqueue(object : Callback<Any>{
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                if (response.isSuccessful){
                    getToDoList()
                }
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                getToDoList()
            }
        })
    }

    fun searchToDoList(keyword: String){
        val retrofit = Retrofit.Builder()
            .baseUrl("http://mellowcode.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val retrofitService = retrofit.create(RetrofitService::class.java)

        val header = HashMap<String, String>()
        val sp = this.getSharedPreferences(
            "user_info",
            Context.MODE_PRIVATE
        )
        val token = sp.getString("token", "")
        header.put("Authorization", "token " + token!!)

        retrofitService.search(header, keyword).enqueue(object : Callback<ArrayList<ToDo>>{
            override fun onResponse(
                call: Call<ArrayList<ToDo>>,
                response: Response<ArrayList<ToDo>>
            ) {
                if (response.isSuccessful){
                    val todoList = response.body()

                    makeToDoList(todoList!!)
                }
            }

            override fun onFailure(call: Call<ArrayList<ToDo>>, t: Throwable) {
            }
        })
    }
}

class ToDoListRecyclerAdapter(
    val todoList: ArrayList<ToDo>,
    val inflater: LayoutInflater,
    val activity: MainActivity
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var previousDate: String = ""

    inner class DateViewHolder(val binding: TodoDateBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val dateTextView: TextView
        val content: TextView
        val isComplete: ImageView

        init {
            dateTextView = binding.date
            content = binding.content
            isComplete = binding.isComplete

            isComplete.setOnClickListener {
                activity.changeToDoComplete(todoList.get(adapterPosition).id)
            }
        }
    }

    inner class ContentViewHolder(val binding: TodoContentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val content: TextView
        val isComplete: ImageView

        init {
            content = binding.content
            isComplete = binding.isComplete

            isComplete.setOnClickListener {
                activity.changeToDoComplete(todoList.get(adapterPosition).id)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val todo = todoList.get(position)
        // 문자열 자르기
        val tempDate = todo.created.split("T")[0]
        if (previousDate == tempDate) {
            return 0
        } else {
            previousDate = tempDate
            return 1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            1 -> {
                val view = inflater.inflate(R.layout.todo_date, parent, false)
                return DateViewHolder(TodoDateBinding.bind(view))
            }
            else -> {
                val view = inflater.inflate(R.layout.todo_content, parent, false)
                return ContentViewHolder(TodoContentBinding.bind(view))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val todo = todoList.get(position)
        if (holder is DateViewHolder) {
            (holder as DateViewHolder).dateTextView.text = todo.created.split("T")[0]
            (holder as DateViewHolder).content.text = todo.content
            if (todo.is_complete) {
                (holder as DateViewHolder).isComplete.setImageDrawable(
                    ContextCompat.getDrawable(
                        activity,
                        R.drawable.btn_radio_check
                    )
                )
            } else {
                (holder as DateViewHolder).isComplete.setImageDrawable(
                    ContextCompat.getDrawable(
                        activity,
                        R.drawable.btn_radio
                    )
                )
            }
        } else {
            (holder as ContentViewHolder).content.text = todo.content
            if (todo.is_complete) {
                (holder as ContentViewHolder).isComplete.setImageDrawable(
                    ContextCompat.getDrawable(
                        activity,
                        R.drawable.btn_radio_check
                    )
                )
            } else {
                (holder as ContentViewHolder).isComplete.setImageDrawable(
                    ContextCompat.getDrawable(
                        activity,
                        R.drawable.btn_radio
                    )
                )
            }
        }
    }

    override fun getItemCount(): Int {
        return todoList.size
    }
}