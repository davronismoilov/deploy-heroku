package uz.oliymahad.userservice.service;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import uz.oliymahad.userservice.dto.admin.GroupSectionDto;
import uz.oliymahad.userservice.dto.request.GroupRequestDto;
import uz.oliymahad.userservice.dto.response.Response;
import uz.oliymahad.userservice.dto.response.RestAPIResponse;
import uz.oliymahad.userservice.dto.response.UserDetailResponse;
import uz.oliymahad.userservice.dto.response.UserResponse;
import uz.oliymahad.userservice.model.entity.UserEntity;
import uz.oliymahad.userservice.model.entity.course.CourseEntity;
import uz.oliymahad.userservice.model.entity.group.GroupEntity;
import uz.oliymahad.userservice.model.enums.EGender;
import uz.oliymahad.userservice.model.enums.GroupStatusEnum;
import uz.oliymahad.userservice.repository.CourseRepository;
import uz.oliymahad.userservice.repository.GroupRepository;
import uz.oliymahad.userservice.repository.QueueRepository;
import uz.oliymahad.userservice.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService implements Response {

    private final GroupRepository groupRepository;

    private final CourseRepository courseRepository;

    private final QueueService queueService;

    private final QueueRepository queueRepository;

    private final ModelMapper modelMapper;

    private final UserService userService;

    private final UserRepository userRepository;

    public RestAPIResponse addGroup(GroupRequestDto groupRequestDto) {
        Optional<CourseEntity> optionalCourse = courseRepository.findById(groupRequestDto.getCourseId());

        if (optionalCourse.isEmpty()) {
            return new RestAPIResponse(COURSE + NOT_FOUND, false, 404);
        }
        GroupEntity groupEntity = new GroupEntity();
        modelMapper.map(groupRequestDto,groupEntity);
        groupEntity.setCourse(optionalCourse.get());
        groupEntity.setGroupStatus(GroupStatusEnum.IN_PROGRESS);
        groupEntity.setGender(EGender.valueOf(groupRequestDto.getGender()));
        groupEntity.setUsers(queueService.getUsers(groupRequestDto.getCourseId(),"PENDING",groupRequestDto.getMembersCount(),groupRequestDto.getGender()));
        groupRepository.save(groupEntity);
        return new RestAPIResponse(SUCCESSFULLY_SAVED, true, 200);
    }

    public RestAPIResponse getUserDetails (long userId) {
        RestAPIResponse apiResponse = userService.getUserDetails(userId);
        if (!apiResponse.isSuccess()) {
            return new RestAPIResponse(USER + NOT_FOUND,false,404);
        }
        return new RestAPIResponse("User Register Details",true,200,apiResponse.getData());
    }

    public RestAPIResponse getGroups(Pageable page) {
        Page<GroupEntity> groupEntities = groupRepository.findAll(page);
        List<GroupSectionDto> list = groupEntities.getContent().size() > 0 ?
                groupEntities.getContent().stream().map(u -> modelMapper.map(u, GroupSectionDto.class)).collect(Collectors.toList()) :
                new ArrayList<>();
        for (GroupSectionDto groupSectionDto : list) {
            Optional<CourseEntity> optionalCourse = courseRepository.findById(groupSectionDto.getCourseId());
            groupSectionDto.setCourseName(optionalCourse.get().getName());
        }
        PageImpl<GroupSectionDto> groupResponseDtos = new PageImpl<>(list, groupEntities.getPageable(), groupEntities.getTotalElements());
        return new RestAPIResponse(DATA_LIST, true, 200, groupResponseDtos);
    }

    public RestAPIResponse  getGroupUsers (Long id) {
        Optional<GroupEntity> optionalGroup = groupRepository.findById(id);
        if (optionalGroup.isEmpty()) {
            return new RestAPIResponse(GROUP + NOT_FOUND, false,404);
        }
        List<UserEntity> userEntities = optionalGroup.get().getUsers();
        List<UserResponse> userResponseList = new ArrayList<>();
        for (UserEntity user : userEntities) {
            UserResponse userResponse = new UserResponse();
            modelMapper.map(user,userResponse);
            userResponseList.add(userResponse);
        }
        return new RestAPIResponse(DATA_LIST, true,200,userResponseList);
    }

    public RestAPIResponse updateGroup (long id, GroupRequestDto groupRequestDto) {
        Optional<GroupEntity> optionalGroup = groupRepository.findById(id);
        if (optionalGroup.isEmpty()) {
            return new RestAPIResponse(GROUP + NOT_FOUND,false,404);
        }
        GroupEntity groupEntity = optionalGroup.get();
        modelMapper.map(groupRequestDto,groupEntity);
        groupRepository.save(groupEntity);
        return new RestAPIResponse(SUCCESSFULLY_UPDATED,true,200);
    }


}
