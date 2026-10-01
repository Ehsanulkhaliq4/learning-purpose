package com.learningpurpose.mediastreamingservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class DtlsParametersDto {
    private List<DtlsFingerprintDto> fingerprints;
    private String role;
}
