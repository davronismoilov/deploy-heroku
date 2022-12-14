package uz.oliymahad.userservice.converter;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import uz.oliymahad.userservice.dto.response.UserDataResponse;
import uz.oliymahad.userservice.model.entity.UserEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public abstract class UserDataModelConverter {
    public static final ModelMapper modelMapper = new ModelMapper();

    public static Page<UserDataResponse> converter(Page<UserEntity> source) {

        List<UserDataResponse> list = source.getContent().size() > 0 ?
            source.getContent().stream().map(u -> modelMapper.map(u, UserDataResponse.class)).collect(Collectors.toList()) :
            new ArrayList<>();
        return new PageImpl<>(list, source.getPageable(), source.getTotalPages());
    }


    public static List<UserDataResponse> convert(List<UserEntity> source) {
        List<UserDataResponse> list = source.size() > 0 ?
            source.stream().map(u -> modelMapper.map(u, UserDataResponse.class)).collect(Collectors.toList()) :
            new ArrayList<>();
        return list;
    }
}
