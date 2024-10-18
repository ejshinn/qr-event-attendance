package com.fullstack405.bitcfinalprojectkotlin.templete.attend

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
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
    lateinit var event: EventDetailData

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

        val userId = intent.getLongExtra("userId",0)
        val eventId = intent.getLongExtra("eventId",0)
        val complete = intent.getCharExtra("complete",'N') // 수료여부
        val userName = intent.getStringExtra("userName")

        val intentComplete =Intent(this,CompleteViewActivity::class.java)


        val url = "http://10.100.105.205:8080/eventImg/"
//        이벤트id로 해당 이벤트 정보만 불러오기
        Client.retrofit.findEventId(eventId).enqueue(object:retrofit2.Callback<EventDetailData>{
            override fun onResponse(call: Call<EventDetailData>, response: Response<EventDetailData>) {
                event = response.body() as EventDetailData
                Log.d("findEventId","${event}")
                binding.dTitle.text = event.eventTitle
                binding.dContent.text = event.eventContent
                binding.dCreateDate.text=event.visibleDate
                binding.dWriter.text = event.posterUserName

                Glide.with(this@AttendDetailActivity)
                    .load(url+event.eventPoster)
                    .into(binding.dImage)


                ////////////// QR 확인
                val cal_today = Calendar.getInstance()
                val cal_s = Calendar.getInstance() // 시작-7
                val cal_sdate = Calendar.getInstance() // 시작일
                val cal_e = Calendar.getInstance()

                cal_today.time = Date()
                val dateFormat =SimpleDateFormat("yyyy-MM-dd")
                val today = dateFormat.format(cal_today.time) // 오늘 날짜 string 타입

                val sd = event.schedules[0].get("eventDate").toString() // 첫째 날
                val ed = event.schedules[event.schedules.size-1].get("eventDate").toString() // 마지막 날

                val startDate: Date? = dateFormat.parse(sd)
                val endDate:Date? = dateFormat.parse(ed)

                cal_s.time = startDate // 시작일 - 7일
                cal_s.add(Calendar.DATE,-7)

                cal_sdate.time = startDate // 시작일
                cal_e.time = endDate // 마지막날

                // 7일전~행사 마지막날 활성화
                // 행사 다음날 비활성화
                // 기본값 = 비활성화
                binding.btnQR.isEnabled = false
                binding.btnQR.setBackgroundColor(Color.parseColor("#D5D5D5"))

                // 대전제를 7일전으로 잡기
                // 오늘 날짜 > 마지막 날짜
                if (cal_today >= cal_s) {
                    // 버튼 활성화 하고
                    binding.btnQR.isEnabled = true
                    binding.btnQR.setBackgroundColor(Color.parseColor("#283eae"))
                    // 클릭 이벤트 활성화
                    binding.btnQR.setOnClickListener {
                        // 큐알 페이지로 이동
                        var intentQR = Intent(this@AttendDetailActivity, QrViewActivity::class.java)
                        intentQR.putExtra("userId", userId)
                        intentQR.putExtra("eventId", eventId)
                        intentQR.putExtra("eventName", event.eventTitle)
                        startActivity(intentQR)
                    }

                    if (cal_today > cal_e) {
                        // 비활성화
                        binding.btnQR.isEnabled = false
                        binding.btnQR.setBackgroundColor(Color.parseColor("#D5D5D5"))
                    }
                }


            }//onResponse
            override fun onFailure(call: Call<EventDetailData>, t: Throwable) {
                Log.d("findEventId","${t.message}")
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


        // 신청취소
        binding.btnCancleApp.setOnClickListener {
            AlertDialog.Builder(this).run{
                setMessage("취소는 행사 하루 전까지 가능합니다.\n신청을 취소 하시겠습니까?")
                setPositiveButton("확인",object:DialogInterface.OnClickListener{
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        Client.retrofit.deleteApplication(eventId, userId).enqueue(object:retrofit2.Callback<Int>{
                            override fun onResponse(call: Call<Int>, response: Response<Int>) {
                                // 취소 성공 2, 실패 1
                                if(response.body() == 2){
                                    Toast.makeText(this@AttendDetailActivity,"취소가 완료되었습니다.",Toast.LENGTH_SHORT).show()
                                    finish()
                                }else{
                                    Toast.makeText(this@AttendDetailActivity,"취소 실패. 다시 시도해주세요.",Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onFailure(call: Call<Int>, t: Throwable) {
                                Toast.makeText(this@AttendDetailActivity,"취소 실패. 다시 시도해주세요.",Toast.LENGTH_SHORT).show()
                                Log.d("deleteApplication","${t.message}")
                            }

                        })// retrofit
                    }

                }) // 확인
                setNegativeButton("닫기",null)
                show()
            }

        }

        // 뒤로가기
        binding.btnBack.setOnClickListener {
            finish()
        }
    }//onCreate


}