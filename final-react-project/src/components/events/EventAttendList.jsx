import EventAttendDay from "./EventAttendDay.jsx";
import { useNavigate, useParams } from "react-router-dom";
import { useState, useEffect } from "react";
import axios from "axios";

function EventAttendList() {
  const { eventId } = useParams();
  const navigate = useNavigate();

  const [eventData, setEventData] = useState({});
  const [dayWiseAttendData, setDayWiseAttendData] = useState({});
  const [eventSchedules, setEventSchedules] = useState([]);

  const [activeTab, setActiveTab] = useState('overall');

  const [totalAttendees, setTotalAttendees] = useState(0);
  const [completedAttendees, setCompletedAttendees] = useState(0);

  const [completionFilter, setCompletionFilter] = useState('all');
  const [roleFilter, setRoleFilter] = useState('all');

  const [nameSearch, setNameSearch] = useState('');

  const [selectedDay, setSelectedDay] = useState('all');

  useEffect(() => {
    axios.get(`http://localhost:8080/event/attendList/${eventId}`)
      .then(res => {
        const eventInfo = res.data;
        setEventData(eventInfo);
        setEventSchedules(eventInfo.eventScheduleDTOList);
        const groupedByDay = {};
        let total = 0;
        let completed = 0;

        eventInfo.attendUserList.forEach(user => {
          total++;
          if (user.eventComp === 'Y') {
            completed++;
          }

          user.attendInfoDTOList.forEach((attendInfo, index) => {
            const day = index + 1;
            if (!groupedByDay[day]) {
              groupedByDay[day] = [];
            }
            groupedByDay[day].push({
              ...user,
              ...attendInfo
            });
          });
        });

        setDayWiseAttendData(groupedByDay);
        setTotalAttendees(total);
        setCompletedAttendees(completed);
      })
      .catch(err => {
        alert("데이터를 불러오는 중 오류 발생: " + err);
      });
  }, [eventId]);

  const handleList = () => {
    navigate('/');
  };

  const overallAttendData = eventData.attendUserList || [];

  const filteredAttendData = overallAttendData.filter(user => {
    const isCompletionMatch = (completionFilter === 'completed' && user.eventComp === 'Y') ||
      (completionFilter === 'notCompleted' && user.eventComp !== 'Y') ||
      (completionFilter === 'all');

    const isRoleMatch = (roleFilter === 'all') ||
      (roleFilter === 'president' && user.role === 'ROLE_PRESIDENT') ||
      (roleFilter === 'secretary' && user.role === 'ROLE_SECRETARY') ||
      (roleFilter === 'regular' && user.role === 'ROLE_REGULAR') ||
      (roleFilter === 'associate' && user.role === 'ROLE_ASSOCIATE') ||
      (roleFilter === 'deleted' && user.role === 'ROLE_DELETE');

    const isNameMatch = user.name.toLowerCase().includes(nameSearch.toLowerCase());

    return isCompletionMatch && isRoleMatch && isNameMatch;
  });

  const filteredDayWiseData = dayWiseAttendData[selectedDay] || [];

  return (
    <section>
      <h4 className="mb-5">참석자 현황 리스트</h4>
      <h4>{eventData.eventTitle}</h4>
      <h5 className="mb-3">정원: <strong>{eventData.maxPeople > 0 ? eventData.maxPeople : '인원수 제한 없음'}</strong></h5>
      <div className="d-flex py-3 justify-content-between">
        <div className="w-50">
          행사기간 : <span className="ms-3 fw-bold">{eventData.startDate} ~ {eventData.endDate}</span>
        </div>
        <div className="w-50">
          행사시간 : <span className="ms-3 fw-bold">{eventData.startTime} ~ {eventData.endTime}</span>
        </div>
      </div>

      <div className="tabs">
        <div
          className={`tab ${activeTab === 'overall' ? 'active' : ''}`}
          onClick={() => setActiveTab('overall')}
        >
          전체 참석자
        </div>
        <div
          className={`tab ${activeTab === 'daily' ? 'active' : ''}`}
          onClick={() => setActiveTab('daily')}
        >
          일차별 참석자
        </div>
      </div>

      {activeTab === 'overall' ? (
        <div>
          <h5>전체 참석자 목록</h5>
          <div className="d-flex justify-content-between align-items-center mb-3">
            <div>
              <span>신청자 수 : <strong>{totalAttendees}</strong></span> |
              <span className="ms-3">수료자 수 : <strong>{completedAttendees}</strong></span>
            </div>
          </div>
          <div className="d-flex mb-3">
            <input
              type="text"
              placeholder="이름 검색"
              value={nameSearch}
              onChange={(e) => setNameSearch(e.target.value)}
              className="form-control me-2"
            />
            <select
              id="completionFilter"
              className="form-select form-select-sm me-2"
              value={completionFilter}
              onChange={(e) => setCompletionFilter(e.target.value)}
            >
              <option value="all">수료여부</option>
              <option value="completed">수료</option>
              <option value="notCompleted">미수료</option>
            </select>
            <select
              id="roleFilter"
              className="form-select form-select-sm"
              value={roleFilter}
              onChange={(e) => setRoleFilter(e.target.value)}
            >
              <option value="all">직위</option>
              <option value="president">협회장</option>
              <option value="secretary">총무</option>
              <option value="regular">정회원</option>
              <option value="associate">준회원</option>
              <option value="deleted">탈퇴회원</option>
            </select>
          </div>
          <table className="table table-custom mb-5 table-hover">
            <thead>
            <tr>
              <th scope="col">번호</th>
              <th scope="col">이름</th>
              <th scope="col">전화번호</th>
              <th scope="col">소속기관</th>
              <th scope="col">직위</th>
              <th scope="col">수료여부</th>
            </tr>
            </thead>
            <tbody>
            {filteredAttendData.map((user, index) => (
              <tr key={index}>
                <td>{index + 1}</td>
                <td>{user.name}</td>
                <td>{user.userPhone}</td>
                <td>{user.userDepart}</td>
                <td>
                  {user.role === 'ROLE_PRESIDENT' && '협회장'}
                  {user.role === 'ROLE_SECRETARY' && '총무'}
                  {user.role === 'ROLE_REGULAR' && '정회원'}
                  {user.role === 'ROLE_ASSOCIATE' && '준회원'}
                  {user.role === 'ROLE_DELETE' && '탈퇴회원'}
                </td>
                <td>
                  {user.eventComp === 'Y' ? '수료' : '미수료'}
                </td>
              </tr>
            ))}
            </tbody>
          </table>
        </div>
      ) : (
        <div>
          <div className="d-flex mb-3">
            <select
              value={selectedDay}
              onChange={(e) => setSelectedDay(e.target.value)}
              className="form-select"
            >
              <option value="all">전체보기</option>
              {Object.keys(dayWiseAttendData).map((day, index) => (
                <option key={index} value={day}>{day}차</option>
              ))}
            </select>
          </div>

          {/* 전체보기일 때 각 일차별로 EventAttendDay를 렌더링 */}
          {selectedDay === 'all' ? (
            Object.keys(dayWiseAttendData).map((day, index) => (
              <EventAttendDay
                key={index}
                day={day}
                attendData={dayWiseAttendData[day]}
                eventStartTime={eventData.startTime}
                eventEndTime={eventData.endTime}
                eventDate={eventSchedules[day - 1]?.eventDate || ''}
                eventSchedules={eventSchedules}
              />
            ))
          ) : (
            <EventAttendDay
              day={selectedDay}
              attendData={filteredDayWiseData}
              eventStartTime={eventData.startTime}
              eventEndTime={eventData.endTime}
              eventDate={eventSchedules[selectedDay - 1]?.eventDate || ''}
              eventSchedules={eventSchedules}
            />
          )}
        </div>
      )}
      <div className="d-flex justify-content-end mt-3">
        <button type="button" className="btn btn-point" onClick={handleList}>목록보기</button>
      </div>
    </section>
  );
}

export default EventAttendList;