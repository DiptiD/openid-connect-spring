package com.gracenote.content.auth.domain.sociallogin;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gracenote.content.auth.persistence.entity.SocialLogin;

public interface SocialLoginRepository extends JpaRepository<SocialLogin,Integer>  {

}
