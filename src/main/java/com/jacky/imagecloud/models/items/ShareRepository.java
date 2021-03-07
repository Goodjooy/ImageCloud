package com.jacky.imagecloud.models.items;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ShareRepository extends JpaRepository<Share,Integer> {
    Share findByUuid(String uuid);
}
