package qna.domain;

import qna.exception.BlankValidateException;
import qna.exception.CannotDeleteException;
import qna.exception.UnAuthenticationException;

import javax.persistence.*;
import java.util.Objects;

@Entity
public class Question extends BaseDateTimeEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Lob
    private String contents;

    @JoinColumn(name = "writer_id", foreignKey = @ForeignKey(name = "fk_question_writer"))
    @ManyToOne(fetch = FetchType.LAZY)
    private User writer;

    @Embedded
    private Answers answers = new Answers();

    @Column(nullable = false)
    private boolean deleted = false;

    protected Question() {
    }

    public Question(String title, String contents) {
        this(null, title, contents);
    }

    public Question(Long id, String title, String contents) {
        this.id = id;
        this.title = title;
        this.contents = contents;
    }

    public static Question createQuestion(String title, String contents, User writer)
            throws UnAuthenticationException, BlankValidateException {
        validate(title, contents, writer);
        Question question = new Question(title, contents);
        question.setWriter(writer);
        return question;
    }

    private static void validate(String title, String contents, User writer)
            throws UnAuthenticationException, BlankValidateException {

        if (Objects.isNull(title) || title.isEmpty()) {
            throw new BlankValidateException("title", title);
        }

        if (Objects.isNull(contents) || contents.isEmpty()) {
            throw new BlankValidateException("contents", contents);
        }

        if (Objects.isNull(writer)) {
            throw new UnAuthenticationException();
        }
    }

    public Question writeBy(User writer) {
        this.writer = writer;
        return this;
    }

    public Answers getAnswers() {
        return answers;
    }

    public boolean isOwner(User writer) {
        return this.writer.equals(writer);
    }

    public void addAnswer(Answer answer) {
        answer.toQuestion(this);
    }

    public Long getId() {
        return id;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public User getWriter() {
        return writer;
    }

    public void setWriter(User writer) {
        this.writer = writer;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", contents='" + contents + '\'' +
                ", writerId=" + writer.getId() +
                ", deleted=" + deleted +
                '}';
    }

    public void delete(User deleter) throws CannotDeleteException {
        validate(deleter);
        answers.delete(deleter);
        setDeleted(true);
    }

    private void validate(User deleter) throws CannotDeleteException {
        if (!isOwner(deleter)) {
            throw new CannotDeleteException("질문을 삭제할 권한이 없습니다.");
        }
    }
}
