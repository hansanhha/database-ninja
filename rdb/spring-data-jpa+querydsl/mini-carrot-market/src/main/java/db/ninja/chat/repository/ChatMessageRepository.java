package db.ninja.chat.repository;


import db.ninja.chat.entity.ChatMessage;
import org.springframework.data.repository.CrudRepository;


public interface ChatMessageRepository extends CrudRepository<ChatMessage, Long>, ChatMessageCustomRepository {

}
