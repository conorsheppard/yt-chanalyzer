package com.youtube.chanalyzer.ytchanneldata;

import lombok.Data;

import java.util.List;

@Data
public class ScrapeResponseListDTO {
    List<ScrapeResponseDTO> responseDTOS;
}
