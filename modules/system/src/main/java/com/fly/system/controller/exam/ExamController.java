package com.fly.system.controller.exam;


import com.fly.common.core.controller.BaseController;
import com.fly.common.core.domain.R;
import com.fly.common.core.domain.TableDataInfo;
import com.fly.system.domain.exam.dto.ExamAddDTO;
import com.fly.system.domain.exam.dto.ExamEditDTO;
import com.fly.system.domain.exam.dto.ExamQueryDTO;
import com.fly.system.domain.exam.dto.ExamQuestAddDTO;
import com.fly.system.domain.exam.vo.ExamDetailVO;
import com.fly.system.service.exam.IExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/exam")
public class ExamController extends BaseController {

    @Autowired
    private IExamService examService;

    //exam/list
    @GetMapping("/list")
    public TableDataInfo list(ExamQueryDTO examQueryDTO) {
        return getTableDataInfo(examService.list(examQueryDTO));
    }

//    @PostMapping("/add")
//    public R<String> add(@RequestBody ExamAddDTO examAddDTO) {
//        return R.ok(examService.add(examAddDTO));
//    }
//
//    @PostMapping("/question/add")
//    public R<Void> questionAdd(@RequestBody ExamQuestAddDTO examQuestAddDTO) {
//        return toR(examService.questionAdd(examQuestAddDTO));
//    }
//
//    @DeleteMapping("/question/delete")
//    public R<Void> questionDelete(Long examId, Long questionId) {
//        return toR(examService.questionDelete(examId, questionId));
//    }
//
//    @GetMapping("/detail")
//    public R<ExamDetailVO> detail(Long examId) {
//        return R.ok(examService.detail(examId));
//    }
//
//    @PutMapping("/edit")
//    public R<Void> edit(@RequestBody ExamEditDTO examEditDTO) {
//        return toR(examService.edit(examEditDTO));
//    }
//
//    @DeleteMapping("/delete")
//    public R<Void> delete(Long examId) {
//        return toR(examService.delete(examId));
//    }
//
//    @PutMapping("/publish")
//    public R<Void> publish(Long examId) {
//        return toR(examService.publish(examId));
//    }
//
//    @PutMapping("/cancelPublish")
//    public R<Void> cancelPublish(Long examId) {
//        return toR(examService.cancelPublish(examId));
//    }
}

