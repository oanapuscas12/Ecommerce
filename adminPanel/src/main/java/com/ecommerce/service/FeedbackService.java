package com.ecommerce.service;

import com.ecommerce.model.Feedback;
import com.ecommerce.repository.FeedbackRepository;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    public void saveFeedback(Feedback feedback) {
        feedbackRepository.save(feedback);
    }

    public Page<Feedback> getAllFeedback(@NonNull Pageable pageable) {
        return feedbackRepository.findAll(pageable);
    }
}