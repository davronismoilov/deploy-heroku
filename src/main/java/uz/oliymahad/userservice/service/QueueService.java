package uz.oliymahad.userservice.service;

import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uz.oliymahad.userservice.dto.request.FilterQueueForGroupsDTO;
import uz.oliymahad.userservice.dto.request.QueueDto;
import uz.oliymahad.userservice.dto.response.*;
import uz.oliymahad.userservice.model.entity.UserEntity;
import uz.oliymahad.userservice.model.entity.course.CourseEntity;
import uz.oliymahad.userservice.model.entity.queue.QueueEntity;
import uz.oliymahad.userservice.model.enums.Status;
import uz.oliymahad.userservice.repository.CourseRepository;
import uz.oliymahad.userservice.repository.QueueRepository;
import uz.oliymahad.userservice.repository.UserRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QueueService implements BaseService<QueueDto, Long, QueueEntity, Pageable>, Response {

    private final QueueRepository queueRepository;
    private final CourseRepository courseRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final UserService userService;

    @Override
    public RestAPIResponse add(QueueDto queueDto) {
//        boolean exist = userFeign.isExist(queueDto.getUserId());
//        if (!exist) {
//            return new ApiResponse<>(USER + NOT_FOUND,false);
//        }
        Optional<CourseEntity> optionalCourse = courseRepository.findById(queueDto.getCourseId());
        if (optionalCourse.isEmpty()) {
            return new RestAPIResponse(COURSE + NOT_FOUND, false, 404);
        }
        QueueEntity queueEntity = new QueueEntity();
        queueEntity.setCourse(optionalCourse.get());
        queueEntity.setStatus(Status.PENDING);
        queueEntity.setUser(userRepository.findById(queueDto.getUserId()).orElseThrow());
        queueRepository.save(queueEntity);
        return new RestAPIResponse(SUCCESSFULLY_SAVED, true, 200);
    }

    @Override
    public RestAPIResponse getList(Pageable page) {
        return new RestAPIResponse(DATA_LIST, true, 200, queueRepository.findAll(page));

    }

    @Override
    public RestAPIResponse get(Long id) {
        Optional<QueueEntity> optionalQueue = queueRepository.findById(id);
        if (optionalQueue.isEmpty()) {
            return new RestAPIResponse(QUEUE + NOT_FOUND, false, 404);
        }
        QueueDto queueDto = modelMapper.map(optionalQueue.get(), QueueDto.class);
        return new RestAPIResponse(QUEUE, true, 200, queueDto);
    }

    @Override
    public RestAPIResponse delete(Long id) {
        Optional<QueueEntity> optionalQueue = queueRepository.findById(id);
        if (optionalQueue.isEmpty()) {
            return new RestAPIResponse(QUEUE + NOT_FOUND, false, 404);
        }
        queueRepository.delete(optionalQueue.get());
        return new RestAPIResponse(SUCCESSFULLY_DELETED, true, 200);
    }

    @Override
    public RestAPIResponse edit(Long id, QueueDto queueDto) {
        Optional<QueueEntity> optionalQueue = queueRepository.findById(id);
        if (optionalQueue.isEmpty()) {
            return new RestAPIResponse(QUEUE + NOT_FOUND, false, 404);
        }
        QueueEntity queueEntity = optionalQueue.get();
        if (queueDto.getAppliedDate() == null)
            queueDto.setAppliedDate(queueEntity.getAppliedDate());
        modelMapper.map(queueDto, queueEntity);
        queueRepository.save(queueEntity);
        return new RestAPIResponse(SUCCESSFULLY_UPDATED, true, 200);
    }

    public RestAPIResponse getUserCourseQueue(Long userId, Long courseId) {
        List<Long> userCourseQueue = queueRepository.getUserCourseQueue(userId, courseId);
        return new RestAPIResponse(SUCCESS, true, 200, userCourseQueue);
    }



    public RestAPIResponse getQueueByFilter(Long userId, String gender, String status, Long courseId, String appliedDate) {
        String appliedDateAfter = null;
        if (appliedDate != null) {
            appliedDateAfter = getDayAfterDay(appliedDate);
        }
        List<QueueEntity> queueByFilter = queueRepository.getQueueByFilter(userId, gender, status, courseId);
        return new RestAPIResponse(SUCCESS, true, 200, queueByFilter);

    }


    private String getDayAfterDay(String day) {
        String sDay = day.substring(0, 10);
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd").parse(sDay);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long l = date.getTime() + 86400000;
        Date date1 = new Date(l);
        String afterDay = new SimpleDateFormat("yyyy-MM-dd").format(date1);
        return afterDay;
    }

    public RestAPIResponse getQueueDetails(Pageable pageable) {

        Page<QueueEntity> page = queueRepository.findAll(pageable);
        QueueUserPageableResponse response = modelMapper.map(pageable, QueueUserPageableResponse.class);
        List<QueueResponse> list = new ArrayList<>();
        page.getContent().forEach(cont -> {
            list.add(new QueueResponse(
                    cont.getId(),
                    cont.getCourse().getName(),
                    cont.getUser().getId(),
                    cont.getUser().getPhoneNumber(),
                    cont.getUser().getEmail(),
                    cont.getUser().getUserRegisterDetails().getFirstName(),
                    cont.getUser().getUserRegisterDetails().getLastName(),
                    cont.getAppliedDate(),
                    null,
                    cont.getStatus()
                    ));
        });
        response.setContent(list);
        return new RestAPIResponse(HttpStatus.OK.name(), true, 200,response);
    }

    private List<QueueUserDetailsDTO> creatingQueueUserDetailsResponse(List<QueueEntity> queues, List<UserDataResponse> users) {
        List<QueueUserDetailsDTO> queueUserDetailsDTOS = new ArrayList<>();

        queues.forEach(queue -> {
            queueUserDetailsDTOS.add(modelMapper.map(queue, QueueUserDetailsDTO.class));
        });
        int index = 0;
        for (QueueEntity queue : queues) {
            UserDataResponse userDataResponse =
                    users.parallelStream().filter(u -> u.getId().equals(queue.getUser().getId()))
                            .findFirst().orElse(new UserDataResponse());
            queueUserDetailsDTOS.get(index++).setUserData(userDataResponse);
        }
        return queueUserDetailsDTOS;
    }
}