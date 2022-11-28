package jmaster.io.statisticservice.controller;

import java.util.List;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jmaster.io.statisticservice.model.StatisticDTO;
import jmaster.io.statisticservice.service.StatisticService;

@RestController
public class StatisticController {
	
	Logger logger =  LoggerFactory.getLogger(this.getClass());

	@Autowired
	private StatisticService statisticService;
	
	@PostMapping("/statistic")
	public StatisticDTO add(@RequestBody StatisticDTO statisticDTO) {
		logger.debug("Add statistic");
		
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//	}
		statisticService.add(statisticDTO);
		return statisticDTO;
	}
	@GetMapping("/statistics")
	public List<StatisticDTO> getAll() {
		logger.debug("Get all statistic");
		return statisticService.getAll();
	}
	
}
