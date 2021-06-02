package qna.domain;

import qna.CannotDeleteException;
import qna.domain.base.BaseEntity;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Objects;

@Table(name = "Question")
@Entity
public class Question extends BaseEntity {
    private static final String DELETE_NOT_ALLOWED = "질문을 삭제할 권한이 없습니다.";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Title title;

    @Embedded
    private Contents contents;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", foreignKey = @ForeignKey(name = "fk_question_writer"))
    private User writer;

    @Embedded
    private Deletion deleted;

    @Embedded
    private Answers answers;

    protected Question() {
    }

    public Question(String title, String contents) {
        this(null, title, contents);
    }

    public Question(Long id, String title, String contents) {
        this.id = id;
        this.title = new Title(title);
        this.contents = new Contents(contents);
        this.answers = new Answers();
        this.deleted = new Deletion();
    }

    public Question writeBy(User writer) {
        this.writer = writer;
        return this;
    }

    public void addAnswer(Answer answer) {
        answer.toQuestion(this);
        this.answers.add(answer);
    }

    public Long getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title.getTitle();
    }

    public String getContents() {
        return this.contents.getContent();
    }

    public User getWriter() {
        return this.writer;
    }

    public boolean isDeleted() {
        return this.deleted.isDeleted();
    }

    public void delete() {
        this.deleted.delete();
    }

    public DeleteHistories delete(User loginUser) {
        checkIsOwner(loginUser);
        this.deleted.delete();
        return createDeleteHistories(loginUser);
    }

    private DeleteHistories createDeleteHistories(User loginUser) {
        DeleteHistories deleteHistories = new DeleteHistories();
        deleteHistories.addHistory(DeleteHistory.ofQuestion(this.id, loginUser));
        return deleteHistories.addAll(this.answers.delete(loginUser));
    }

    private void checkIsOwner(User loginUser) {
        if (!this.writer.equals(loginUser)) {
            throw new CannotDeleteException(DELETE_NOT_ALLOWED);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Question)) return false;
        Question question = (Question) o;
        return Objects.equals(getId(), question.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", contents='" + contents + '\'' +
                ", writer=" + writer +
                ", deleted=" + deleted +
                ", answers=" + answers +
                '}';
    }
}
