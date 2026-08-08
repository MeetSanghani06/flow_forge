package com.flowforge.backend.health;

import com.flowforge.backend.common.persistence.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "system_info")
@Getter
@Setter
public class SystemInfo extends BaseEntity {

    private String applicationName;

    private String version;
}
