package com.scut.mailsystem.service.settings;

import com.scut.mailsystem.dto.settings.UserSettingsUpdateRequest;
import com.scut.mailsystem.vo.settings.UserSettingsVO;

public interface UserSettingsService {

    UserSettingsVO getSettings(String authorizationHeader);

    UserSettingsVO updateSettings(String authorizationHeader, UserSettingsUpdateRequest request);
}
