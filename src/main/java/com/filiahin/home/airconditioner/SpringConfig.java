package com.filiahin.home.airconditioner;

import com.filiahin.home.airconditioner.binding.GreeDeviceBinderService;
import com.filiahin.home.airconditioner.communication.GreeCommunicationService;
import com.filiahin.home.airconditioner.services.GreeAirconditionerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfig {

    @Bean
    public GreeCommunicationService communicationService() {
        return new GreeCommunicationService();
    }

    @Bean
    public GreeDeviceBinderService binderService() {
        return new GreeDeviceBinderService(communicationService());
    }

    @Bean
    public GreeAirconditionerService airconditionerService() {
        GreeAirconditionerService greeAirconditionerService = new GreeAirconditionerService(binderService(), communicationService());
        greeAirconditionerService.discoverDevices();
        return greeAirconditionerService;
    }

}
