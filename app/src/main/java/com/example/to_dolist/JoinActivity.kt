package com.example.to_dolist

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import com.example.to_dolist.databinding.ActivityJoinBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class JoinActivity : AppCompatActivity() {

    lateinit var binding: ActivityJoinBinding

    var username: String = ""
    var password: String = ""
    var passwordCheck: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityJoinBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.idInput.doAfterTextChanged {
            username = it.toString()
        }

        binding.pwInput.doAfterTextChanged {
            password = it.toString()
        }

        binding.pwReInput.doAfterTextChanged {
            passwordCheck = it.toString()
        }

        binding.pwReInput.addTextChangedListener(object : TextWatcher {
            // 비밀번호 일치하는지 확인
            override fun afterTextChanged(p0: Editable?) {
                if (password.equals(passwordCheck)) {
                    binding.pwConfirm.setText("비밀번호가 일치합니다.")
                    binding.pwConfirm.setTextColor(Color.GREEN)

                    // 가입하기 버튼 활성화
                    binding.signBtn.isEnabled = true

                } else {
                    binding.pwConfirm.setText("비밀번호가 일치하지 않습니다.")
                    binding.pwConfirm.setTextColor(Color.RED)

                    // 가입하기 버튼 비활성화
                    binding.signBtn.isEnabled = false
                }
            }

            //입력하기 전
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            //텍스트 변화가 있을 시
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (password.equals(passwordCheck)) {
                    binding.pwConfirm.setText("비밀번호가 일치합니다.")
                    binding.pwConfirm.setTextColor(Color.GREEN)

                    // 가입하기 버튼 활성화
                    binding.signBtn.isEnabled = true

                } else {
                    binding.pwConfirm.setText("비밀번호가 일치하지 않습니다.")
                    binding.pwConfirm.setTextColor(Color.RED)

                    // 가입하기 버튼 비활성화
                    binding.signBtn.isEnabled = false
                }
            }
        })

        val retrofit = Retrofit.Builder()
            .baseUrl("http://mellowcode.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val retrofitService = retrofit.create(RetrofitService::class.java)

        binding.signBtn.setOnClickListener {
            val user = HashMap<String, Any>()
            user.put("username", username)
            user.put("password1", password)
            user.put("password2", passwordCheck)
            retrofitService.join(user).enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    if (response.isSuccessful) {
                        val user: User = response.body()!!
                        val sharedPreferences =
                            getSharedPreferences("user_info", Context.MODE_PRIVATE)
                        val editor: SharedPreferences.Editor = sharedPreferences.edit()
                        editor.putString("token", user.token)
                        editor.putString("user_id", user.id.toString())
                        editor.commit()

                        val intent = Intent(this@JoinActivity, LoginActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@JoinActivity, "가입에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<User>, t: Throwable) {
                    Toast.makeText(this@JoinActivity, "가입에 실패했습니다.", Toast.LENGTH_SHORT).show()

                }
            })
        }
    }
}