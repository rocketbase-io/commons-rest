package io.rocketbase.sample.converter;

import io.rocketbase.commons.converter.EntityReadWriteConverter;
import io.rocketbase.sample.dto.employee.EmployeeRead;
import io.rocketbase.sample.dto.employee.EmployeeWrite;
import io.rocketbase.sample.model.EmployeeEntity;
import io.rocketbase.sample.repository.mongo.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmployeeConverter implements EntityReadWriteConverter<EmployeeEntity, EmployeeRead, EmployeeWrite> {

    private final CompanyRepository companyRepository;
    private final CompanyConverter companyConverter;

    @Override
    public EmployeeRead fromEntity(EmployeeEntity entity) {
        if (entity == null) {
            return null;
        }
        return EmployeeRead.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .dateOfBirth(entity.getDateOfBirth())
                .female(entity.isFemale())
                .email(entity.getEmail())
                .company(entity.getCompanyId() != null
                    ? companyConverter.fromEntity(companyRepository.findById(entity.getCompanyId()).orElse(null))
                    : null)
                .build();
    }

    @Override
    public EmployeeEntity newEntity(EmployeeWrite write) {
        if (write == null) {
            return null;
        }
        return EmployeeEntity.builder()
                .firstName(write.getFirstName())
                .lastName(write.getLastName())
                .dateOfBirth(write.getDateOfBirth())
                .female(write.isFemale())
                .email(write.getEmail())
                .build();
    }

    @Override
    public EmployeeEntity updateEntityFromEdit(EmployeeWrite write, EmployeeEntity entity) {
        if (write == null || entity == null) {
            return entity;
        }
        entity.setFirstName(write.getFirstName());
        entity.setLastName(write.getLastName());
        entity.setDateOfBirth(write.getDateOfBirth());
        entity.setFemale(write.isFemale());
        entity.setEmail(write.getEmail());
        return entity;
    }
}
