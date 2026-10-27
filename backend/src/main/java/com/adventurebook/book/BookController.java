package com.adventurebook.book;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public List<BookSummary> list(@RequestParam(required = false) String search,
                                  @RequestParam(name = "difficulty", required = false) List<Difficulty> difficulties,
                                  @RequestParam(name = "category", required = false) List<String> categories) {
        return bookService.search(new BookSearch(search, difficulties, categories));
    }
}
