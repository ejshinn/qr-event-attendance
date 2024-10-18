package com.fullstack405.bitcfinalprojectkotlin.templete.attend

import android.content.Intent
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.fullstack405.bitcfinalprojectkotlin.R
import com.fullstack405.bitcfinalprojectkotlin.client.Client
import com.fullstack405.bitcfinalprojectkotlin.data.EventDetailData
import com.fullstack405.bitcfinalprojectkotlin.data.EventListData
import com.fullstack405.bitcfinalprojectkotlin.databinding.ActivityAttendDetailBinding
import retrofit2.Call
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date

class AttendDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
//        setContentView(R.layout.activity_attend_detail)
        val binding = ActivityAttendDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        var userId = intent.getLongExtra("userId",0)
        var eventId = intent.getLongExtra("eventId",0)
        var complete = intent.getCharExtra("complete",'N') // 수료여부
        var userName = intent.getStringExtra("userName")

        var intentComplete =Intent(this,CompleteViewActivity::class.java)

        lateinit var event: EventDetailData
        var url = "http://10.100.105.205:8080/eventImg/"
//        이벤트id로 해당 이벤트 정보만 불러오기
        Client.retrofit.findEventId(eventId).enqueue(object:retrofit2.Callback<EventDetailData>{
            override fun onResponse(call: Call<EventDetailData>, response: Response<EventDetailData>) {
                event = response.body() as EventDetailData
                binding.dTitle.text = event.eventTitle
                binding.dContent.text = event.eventContent
                binding.dCreateDate.text=event.visibleDate
                binding.dWriter.text = event.posterUserName

                Glide.with(this@AttendDetailActivity)
                    .load(url+event.eventPoster)
                    .into(binding.dImage)

            }

            override fun onFailure(call: Call<EventDetailData>, t: Throwable) {
                Log.d("eventDetail error","eventDetail load error")
            }
        }) // retrofit

        // Y 이면 활성화 N이면 비활성화
        // 참석증 확인
        if(complete == 'Y'){
            binding.btnComplete.isEnabled = true
            binding.btnComplete.setOnClickListener {
                // 참석증 페이지로 이동
                intentComplete.putExtra("userId",userId)
                intentComplete.putExtra("eventId",eventId)
                intentComplete.putExtra("userName",userName)
                startActivity(intentComplete)
            }
        }
        else{
            binding.btnComplete.isEnabled = false
            binding.btnComplete.setBackgroundColor(Color.parseColor("#D5D5D5"))
        }


        // QR 확인
        // 마지막 날짜가 지나거나, 마지막 날의 입장/퇴장 시간이 다 찍힌 경우= 1 비활성화, 아님 2
        binding.btnQR.setOnClickListener {
            // 큐알 페이지로 이동
            var intentQR = Intent(this, QrViewActivity::class.java)
            intentQR.putExtra("userId", userId)
            intentQR.putExtra("eventId", eventId)
            intentQR.putExtra("eventName",event.eventTitle)
            startActivity(intentQR)
        }
//        } // if

        // 신청취소
        binding.btnCancleApp.setOnClickListener { 
            
        }

        // 뒤로가기
        binding.btnBack.setOnClickListener {
            finish()
        }
    }//onCreate


}