package com.mooko.dev.controller;

import com.mooko.dev.domain.PrincipalDetails;
import com.mooko.dev.domain.User;
import com.mooko.dev.dto.day.req.BarcodeDateDto;
import com.mooko.dev.dto.day.req.CalendarReqDto;
import com.mooko.dev.dto.day.req.DayPhotoDto;
import com.mooko.dev.dto.day.res.CalendarResDto;
import com.mooko.dev.dto.day.res.DayDto;
import com.mooko.dev.dto.event.res.BarcodeIdDto;
import com.mooko.dev.facade.AggregationFacade;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/user")
public class DayController {
    private final AggregationFacade aggregationFacade;

    @GetMapping("/calender")
    public ResponseEntity<CalendarResDto> showCalendar(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody CalendarReqDto calendarReqDto){

        User user = principalDetails.getUser();

        CalendarResDto thumbnailInfoList = aggregationFacade.showCalendar(user,calendarReqDto);
        return ResponseEntity.ok(thumbnailInfoList);
    }

    @GetMapping("{date}")
    public ResponseEntity<DayDto> showDay(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable String date){
        User user = principalDetails.getUser();

        DayDto dayDto = aggregationFacade.showDay(user,date);
        return ResponseEntity.ok(dayDto);
    }

    @PostMapping("{date}")
    public ResponseEntity<Void> updateDay(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @PathVariable String date,
            @ModelAttribute DayPhotoDto dayPhotoDto){
        User user = principalDetails.getUser();

        aggregationFacade.updateDay(user,date,dayPhotoDto);

        return ResponseEntity.status(HttpStatus.OK).build();

    }

    @PostMapping("/new-barcode")
    public ResponseEntity<BarcodeIdDto> makeNewBarcode(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestBody BarcodeDateDto barcodeDateDto
    ) throws IOException {
        User user = principalDetails.getUser();
        Long barcodeId = aggregationFacade.makeNewDayBarcode(user, barcodeDateDto);
        return ResponseEntity.ok(BarcodeIdDto.builder().barcodeId(barcodeId.toString()).build());
    }
}
