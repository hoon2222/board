package com.example.board.controller;

import com.example.board.dto.Board;
import com.example.board.dto.LoginInfo;
import com.example.board.service.BoardService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;
    @GetMapping("/")
    public String list(@RequestParam(name="page", defaultValue = "1") int page, HttpSession session, Model model){ // HttpSession, Model은 Spring이 자동으로 넣어준다.
        // 게시물 목록을 읽어온다. 페이징 처리한다.
        LoginInfo loginInfo = (LoginInfo)session.getAttribute("loginInfo");
        model.addAttribute("loginInfo", loginInfo); // 템플릿에게

        int totalCount = boardService.getTotalCount(); // 11
        List<Board> list = boardService.getBoards(page); // page가 1,2,3,4 ....
        int pageCount = totalCount / 10; // 1
        if(totalCount % 10 > 0){ // 나머지가 있을 경우 1page를 추가
            pageCount++;
        }
        int currentPage = page;
//        System.out.println("totalCount : " + totalCount);
//        for(Board board : list){
//            System.out.println(board);
//        }
        model.addAttribute("list", list);
        model.addAttribute("pageCount", pageCount);
        model.addAttribute("currentPage", currentPage);
        return "list";
    }
    @GetMapping("/board")
    public String board(
            @RequestParam("boardId") int boardId,
            Model model,
            HttpSession httpSession
    ){
        LoginInfo loginInfo = (LoginInfo)httpSession.getAttribute("loginInfo");
        model.addAttribute("loginInfo", loginInfo); // 템플릿에게

        System.out.println("loginInfo : " + loginInfo);

        Board board = boardService.getBoard(boardId, true);
        model.addAttribute("board", board);
        return "board";
    }

    @GetMapping("writeForm")
    public String writeForm(HttpSession session, Model model){
        //로그인한 사람만 글 쓸수있게함
        LoginInfo loginInfo = (LoginInfo)session.getAttribute("loginInfo");
        model.addAttribute("loginInfo", loginInfo); //템플릿에 값전달
        return "writeForm";
    }

    @PostMapping("/write")
    public String write(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            HttpSession session
    ){
        LoginInfo loginInfo = (LoginInfo)session.getAttribute("loginInfo");
        if(loginInfo == null){ // 세션에 로그인 정보가 없으면 /loginform으로 redirect
            return "redirect:/loginform";
        }
        // 로그인한 사용자만 글을 써야한다.
        // 세션에서 로그인한 정보를 읽어들인다. 로그인을 하지 않았다면 리스트보기로 자동 이동 시킨다.
        System.out.println("title : " + title);
        // 로그인 한 회원 정보 + 제목, 내용을 저장한다.System.out.println("content : " + content);

        boardService.addBoard(loginInfo.getUserId(), title, content);

        return "redirect:/"; // 리스트 보기로 리다이렉트한다.
    }

    @GetMapping("/updateform")
    public String updateform(@RequestParam("boardId") int boardId, Model model,  HttpSession session){
        LoginInfo loginInfo = (LoginInfo) session.getAttribute("loginInfo");
        if (loginInfo == null) { // 세션에 로그인 정보가 없으면 /loginform으로 redirect
            return "redirect:/loginform";
        }
        // boardId에 해당하는 정보를 읽어와서 updateform 템플릿에게 전달한다.
        Board board = boardService.getBoard(boardId, false);
        model.addAttribute("board", board);
        model.addAttribute("loginInfo", loginInfo);
        return "updateform";
    }

    @PostMapping("/update")
    public String update(@RequestParam("boardId") int boardId,
                         @RequestParam("title") String title,
                         @RequestParam("content") String content,
                         HttpSession session
    ){

        LoginInfo loginInfo = (LoginInfo) session.getAttribute("loginInfo");
        if (loginInfo == null) { // 세션에 로그인 정보가 없으면 /loginform으로 redirect
            return "redirect:/loginform";
        }

        Board board = boardService.getBoard(boardId, false);
        if(board.getUserId() != loginInfo.getUserId()){
            return "redirect:/board?boardId=" + boardId; // 글보기로 이동한다.
        }
        // boardId에 해당하는 글의 제목과 내용을 수정한다.
        boardService.updateBoard(boardId, title, content);
        return "redirect:/board?boardId=" + boardId; // 수정된 글 보기로 리다이렉트한다.
    }

    @GetMapping("/delete")
    public String delete(
            @RequestParam("boardId") int boardId,
            HttpSession session
    ) {
        LoginInfo loginInfo = (LoginInfo) session.getAttribute("loginInfo");
        if (loginInfo == null) { // 세션에 로그인 정보가 없으면 /loginform으로 redirect
            return "redirect:/loginform";
        }

        // loginInfo.getUserId() 사용자가 쓴 글일 경우에만 삭제한다.
        List<String> roles = loginInfo.getRoles();
        if(roles.contains("ROLE_ADMIN")){
            boardService.deleteBoard(boardId);
        }else {
            boardService.deleteBoard(loginInfo.getUserId(), boardId);
        }

        return "redirect:/"; // 리스트 보기로 리다이렉트한다.
    }
}
