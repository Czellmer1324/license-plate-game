package com.czellmer1324.licenseplategame.services;

import com.czellmer1324.licenseplategame.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository;

}
