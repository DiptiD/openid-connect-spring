package com.gracenote.content.auth.domain.history;

import com.gracenote.content.auth.persistence.entity.UserApplicationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * This is responsible for interacting with db
 *
 * @author deepak on 6/10/17.
 */
public interface UserApplicationHistoryRepository extends JpaRepository <UserApplicationHistory, Integer> {

}
