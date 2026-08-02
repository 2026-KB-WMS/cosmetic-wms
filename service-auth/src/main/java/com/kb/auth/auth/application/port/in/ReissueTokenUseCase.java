package com.kb.auth.auth.application.port.in;

import com.kb.auth.auth.application.port.in.dto.ReissueCommand;
import com.kb.auth.auth.application.port.in.dto.ReissueResult;

public interface ReissueTokenUseCase {

    ReissueResult reissue(ReissueCommand command);
}
