package com.czellmer1324.licenseplategame.dto;

import java.time.ZonedDateTime;

public record GetGroupsDTO(String groupName, String groupOwnerUserName, long groupId, ZonedDateTime endDate) {
}
