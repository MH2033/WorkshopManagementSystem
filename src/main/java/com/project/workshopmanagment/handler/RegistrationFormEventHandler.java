package com.project.workshopmanagment.handler;

import com.project.workshopmanagment.entity.RegistrationForm;
import com.project.workshopmanagment.entity.workshop.TakenWorkshop;
import com.project.workshopmanagment.entity.workshop.WorkshopRelation;
import com.project.workshopmanagment.entity.enums.WorkshopRelationType;
import com.project.workshopmanagment.entity.enums.WorkshopState;
import com.project.workshopmanagment.repository.wokrshop.TakenWorkshopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;

import javax.validation.Valid;
import java.util.Date;

@RepositoryEventHandler
public class RegistrationFormEventHandler {
    @Autowired
    TakenWorkshopRepository takenWorkshopRepository;

    //TODO: Adding validation for in-conflict and dependent workshops

    @HandleBeforeCreate
    public void handleRegistrationFormBeforeSave(@Valid RegistrationForm registrationForm){

        if(registrationForm.getDesiredWorkshop().getCapacity() < 1){
            throw new RuntimeException("Workshop is full!");
        }

        for(RegistrationForm r: registrationForm.getParticipant().getRegistrationForms()){
            for (WorkshopRelation j : registrationForm.getDesiredWorkshop().getWorkshop().getWorkshopRelation())
                if (j.getWorkshop().equals(r.getTakenWorkshop().getWorkshopGroup().getOfferedWorkshop().getWorkshop())) {
                    if(j.getWorkshopRelationType().equals(WorkshopRelationType.PRE_REQUISITE)){
                        if(!r.getTakenWorkshop().getWorkshopState().equals(WorkshopState.PASS))
                            throw new RuntimeException("Pre requisites have not met");
                    }
                }
            if(registrationForm.getDesiredWorkshop().getWorkshop().equals(r.getDesiredWorkshop().getWorkshop())){
                throw new RuntimeException("Participant has already enrolled in the workshop");
            }
            for(Date d1: r.getDesiredWorkshop().getOfferingDatesAndTimes()){
                for(Date d2: registrationForm.getDesiredWorkshop().getOfferingDatesAndTimes()){
                    if(d1.compareTo(d2) == 0) {
                        throw new RuntimeException("Selected Workshop is in time conflict with your previous workshops");
                    }
                    else if(d1.compareTo(d2) > 0 &&
                            d1.toInstant().isBefore(d2.toInstant().plusSeconds(registrationForm.getDesiredWorkshop().getDuration().getSeconds()))){
                        throw new RuntimeException("Selected Workshop is in time conflict with your previous workshops");
                    }
                    else if(d2.compareTo(d1) > 0 &&
                            d2.toInstant().isBefore(d1.toInstant().plusSeconds(registrationForm.getDesiredWorkshop().getDuration().getSeconds()))){
                        throw new RuntimeException("Selected Workshop is in time conflict with your previous workshops");
                    }
                }
            }
        }
    }
    @HandleAfterCreate
    public void handleRegistrationFormAfterSave(@Valid RegistrationForm registrationForm){

        TakenWorkshop tw = new TakenWorkshop();
        tw.setRegistrationForm(registrationForm);
        tw.setWorkshopGroup(registrationForm.getDesiredWorkshop().getWorkshopGroups().get(0));
        tw.setWorkshopState(WorkshopState.ONPERFORMING);
        registrationForm.getDesiredWorkshop().setCapacity(registrationForm.getDesiredWorkshop().getCapacity() - 1);
        takenWorkshopRepository.save(tw);
        registrationForm.setTakenWorkshop(tw);
    }

    @HandleAfterDelete
    public void handleRegistrationFormAfterDelete(@Valid RegistrationForm registrationForm){
        registrationForm.getDesiredWorkshop().setCapacity(registrationForm.getDesiredWorkshop().getCapacity() + 1);
    }
}
