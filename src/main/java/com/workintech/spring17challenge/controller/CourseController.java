package com.workintech.spring17challenge.controller;

import com.workintech.spring17challenge.exceptions.ApiException;
import com.workintech.spring17challenge.entity.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/courses")
@CrossOrigin(origins = "*")
public class CourseController {

    private List<Course> courses;

    private final CourseGpa lowCourseGpa;
    private final CourseGpa mediumCourseGpa;
    private final CourseGpa highCourseGpa;

    @Autowired
    public CourseController(CourseGpa lowCourseGpa, CourseGpa mediumCourseGpa, CourseGpa highCourseGpa) {
        this.lowCourseGpa = lowCourseGpa;
        this.mediumCourseGpa = mediumCourseGpa;
        this.highCourseGpa = highCourseGpa;
    }

    @PostConstruct
    public void init() {
        courses = new ArrayList<>();
    }

    @GetMapping
    public List<Course> getAll() {
        return this.courses;
    }

    @GetMapping("/{name}")
    public Course getByName(@PathVariable String name) {
        return courses.stream()
                .filter(course -> course.getName() != null && course.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new ApiException("Course not found with name: " + name, HttpStatus.NOT_FOUND));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseResponse save(@RequestBody Course course) {
        if (course.getCredit() == null || course.getCredit() < 0 || course.getCredit() > 4) {
            throw new ApiException("Credit must be between 0 and 4", HttpStatus.BAD_REQUEST);
        }

        courses.add(course);
        double totalGpa = calculateGpa(course);
        return new CourseResponse(course, totalGpa);
    }

    @PutMapping("/{id}")
    public CourseResponse update(@PathVariable int id, @RequestBody Course course) {
        Course existingCourse = courses.stream()
                .filter(c -> c.getId() != null && c.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ApiException("Course not found with id: " + id, HttpStatus.NOT_FOUND));

        if (course.getCredit() == null || course.getCredit() < 0 || course.getCredit() > 4) {
            throw new ApiException("Credit must be between 0 and 4", HttpStatus.BAD_REQUEST);
        }

        int index = courses.indexOf(existingCourse);
        course.setId(id);
        courses.set(index, course);
        double totalGpa = calculateGpa(course);
        return new CourseResponse(course, totalGpa);
    }

    @DeleteMapping("/{id}")
    public Course delete(@PathVariable int id) {
        Course existingCourse = courses.stream()
                .filter(c -> c.getId() != null && c.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ApiException("Course not found with id: " + id, HttpStatus.NOT_FOUND));

        courses.remove(existingCourse);
        return existingCourse;
    }

    private double calculateGpa(Course course) {
        int credit = course.getCredit() != null ? course.getCredit() : 0;
        int coefficient = (course.getGrade() != null) ? course.getGrade().getCoefficient() : 0;

        if (credit <= 2) {
            return coefficient * credit * lowCourseGpa.getGpa();
        } else if (credit == 3) {
            return coefficient * credit * mediumCourseGpa.getGpa();
        } else if (credit == 4) {
            return coefficient * credit * highCourseGpa.getGpa();
        }
        return 0;
    }
}