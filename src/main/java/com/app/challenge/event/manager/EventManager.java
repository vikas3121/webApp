package com.app.challenge.event.manager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.app.challenge.domain.Challenge;
import com.app.challenge.domain.Player;
import com.app.challenge.domain.UserAccount;
import com.app.challenge.event.dao.ChallengeDomain;
import com.app.challenge.event.dao.EventManagerDao;
import com.app.challenge.event.vo.AllChallengeResponseVO;
import com.app.challenge.event.vo.AppResponseVO;
import com.app.challenge.event.vo.ChallengeVO;
import com.app.challenge.event.vo.CommentVO;
import com.app.challenge.event.vo.CommentsResponseVO;
import com.app.challenge.event.vo.LikeResponseVO;
import com.app.challenge.event.vo.RegisterResponseVO;
import com.app.challenge.event.vo.UserAccountVO;
import com.app.challenge.fbutil.FacebookClientHandler;
import com.app.quartz.RivalScheduledJob;
import com.app.quartz.RivalsAppScheduler;

@Component
public class EventManager {

	@Autowired
	private EventManagerDao eventManagerDao;

	@Autowired
	private FacebookClientHandler fbClientHandler;

	@Autowired
	private RivalsAppScheduler rivalsAppScheduler;

	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Autowired
	RivalScheduledJob rivalScheduledJob;

	@Transactional(rollbackFor = SQLException.class)
	public AppResponseVO registerNewUserId(String token, String email) throws SQLException {
		AppResponseVO response = new AppResponseVO();
		String responseMessage;
		try {
			responseMessage = eventManagerDao.createNewUserId(email, token);
		} catch (SQLException e) {
			throw e;
		}
		response.setResponseMessage(responseMessage);
		return response;
	}

	@Transactional(rollbackFor = SQLException.class)
	public AppResponseVO updateToken(String token, String email) throws SQLException {
		AppResponseVO response = new AppResponseVO();
		String responseMessage;
		try {
			responseMessage = eventManagerDao.createNewUserId(email, token);
		} catch (SQLException e) {
			throw e;
		}
		response.setResponseMessage(responseMessage);
		return response;
	}

	@Transactional(rollbackFor = SQLException.class)
	public AppResponseVO updateUserToken(Long uid, String userEmail, String token, String userName)
			throws SQLException {
		AppResponseVO response = new AppResponseVO();
		try {
			eventManagerDao.updateUserToken(uid, userEmail, token, userName);
		} catch (Exception e) {
			throw new SQLException();
		}

		response.setResponseMessage("success");
		return response;
	}

	@Transactional(rollbackFor = SQLException.class)
	public RegisterResponseVO registerDevice(UserAccountVO userAccountVO) throws SQLException {
		UserAccount uac = new UserAccount();
		BeanUtils.copyProperties(userAccountVO, uac);
		RegisterResponseVO responseVO;
		long count = 0;
		try {
			responseVO = eventManagerDao.registerDevice(uac);
			count = eventManagerDao.fetchAllChallenges(0, null, true).size();
			responseVO.setActiveCount(count);
		} catch (Exception e) {
			throw new SQLException();
		}
		return responseVO;
	}

	public List<AllChallengeResponseVO> fetchAllChallenges(int challengeFrom) {
		List<AllChallengeResponseVO> responseList = new ArrayList<AllChallengeResponseVO>();
		AllChallengeResponseVO responseVO = new AllChallengeResponseVO();
		List<ChallengeDomain> challenges = new ArrayList<>();
		try {
			challenges = eventManagerDao.fetchAllChallenges(challengeFrom, "COMPLETED");
			List<Player> players = null;
			Player player = null;
			if (challenges != null) {
				Iterator<ChallengeDomain> iter = challenges.iterator();
				ChallengeDomain domain = null;
				List<Long> ids = null;
				Map<Long, List<String>> commentMap = new HashMap<>();
				while (iter.hasNext()) {
					ids = new ArrayList<>();

					responseVO = new AllChallengeResponseVO();
					domain = iter.next();

					long id = domain.getChallengeId();
					ids.add(id);
					try {
						commentMap = eventManagerDao.fetchCommentsForChallenges(ids);
						domain.setComments(commentMap.containsKey(id) ? commentMap.get(id) : new ArrayList<String>());
					} catch (Exception e) {
						e.printStackTrace();
						domain.setComments(new ArrayList<String>());
					}
					BeanUtils.copyProperties(domain, responseVO);
					players = eventManagerDao.fetchPlayersOfChallenges(id);

					if (players != null && !players.isEmpty()) {

						Iterator<Player> iters = players.iterator();
						int i = 0;
						while (iters.hasNext()) {
							player = iters.next();
							if (i++ == 0)
								responseVO.setPlayer1(player);
							else
								responseVO.setPlayer2(player);
						}

					}
					responseList.add(responseVO);
				}

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return responseList;
	}

	public List<AllChallengeResponseVO> fetchMyChallenges(long userID, int challengeFrom) {

		List<AllChallengeResponseVO> responseList = new ArrayList<AllChallengeResponseVO>();
		AllChallengeResponseVO responseVO = new AllChallengeResponseVO();
		List<ChallengeDomain> challenges = new ArrayList<>();
		try {
			challenges = eventManagerDao.fetchMyChallenges(challengeFrom, userID);
			List<Player> players = null;
			Player player = null;
			Set<Long> userIdSet = new HashSet<>();
			for (ChallengeDomain challenge : challenges) {

				long creatorId = challenge.getCreatorId();
				long acceptorId = challenge.getAcceptorId();
				userIdSet.add(creatorId);
				userIdSet.add(acceptorId);
			}

			ArrayList<Long> userIdList = new ArrayList<>();
			userIdList.addAll(userIdSet);
			Map<Long, UserAccount> userDetailsMap = eventManagerDao.fetchUserDetails(userIdList);

			if (challenges != null) {
				Iterator<ChallengeDomain> iter = challenges.iterator();
				ChallengeDomain domain = null;
				List<Long> ids = null;
				Map<Long, List<String>> commentMap = new HashMap<>();
				while (iter.hasNext()) {
					responseVO = new AllChallengeResponseVO();
					domain = iter.next();
					long id = domain.getChallengeId();
					ids = new ArrayList<>();
					ids.add(id);
					try {
						commentMap = eventManagerDao.fetchCommentsForChallenges(ids);
						domain.setComments(commentMap.containsKey(id) ? commentMap.get(id) : new ArrayList<String>());
					} catch (Exception e) {
						e.printStackTrace();
						domain.setComments(new ArrayList<String>());
					}

					long creatorId = domain.getCreatorId();
					long acceptorId = domain.getAcceptorId();
					UserAccount userAccountCreator = userDetailsMap.get(creatorId);
					String acceptorImage = "no image";
					UserAccount acceptorDetail = new UserAccount();
					if (acceptorId > 0) {
						acceptorDetail = userDetailsMap.get(acceptorId);
						acceptorImage = acceptorDetail.getUserImage();
					}

					String creatorImage = userAccountCreator.getUserImage();

					responseVO.setCreatorImage(creatorImage);
					responseVO.setAcceptorImage(acceptorImage);
					responseVO.setAcceptorId(acceptorId);
					responseVO.setCreatorId(creatorId);
					responseVO
							.setAcceptorName(acceptorDetail.getUserName() == null ? "" : acceptorDetail.getUserName());
					responseVO.setCreatorName(userAccountCreator.getUserName());
					BeanUtils.copyProperties(domain, responseVO);
					players = eventManagerDao.fetchPlayersOfChallenges(id);
					if (players != null && !players.isEmpty()) {

						Iterator<Player> iters = players.iterator();
						int i = 0;
						while (iters.hasNext()) {
							player = iters.next();
							if (i++ == 0)
								responseVO.setPlayer1(player);
							else
								responseVO.setPlayer2(player);
						}

					}
					responseList.add(responseVO);
				}

			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return responseList;
	}

	public List<AllChallengeResponseVO> fetchActiveChallenges(int challengeFrom) {

		List<AllChallengeResponseVO> responseList = new ArrayList<AllChallengeResponseVO>();
		AllChallengeResponseVO responseVO = new AllChallengeResponseVO();
		List<ChallengeDomain> challenges = new ArrayList<>();
		try {
			challenges = eventManagerDao.fetchAllChallenges(challengeFrom, "INPROGRESS");
			List<Player> players = null;
			Player player = null;
			if (challenges != null) {
				Iterator<ChallengeDomain> iter = challenges.iterator();
				ChallengeDomain domain = null;
				List<Long> ids = null;
				Map<Long, List<String>> commentMap = new HashMap<>();
				while (iter.hasNext()) {
					responseVO = new AllChallengeResponseVO();
					domain = iter.next();
					long id = domain.getChallengeId();
					ids = new ArrayList<>();
					ids.add(id);
					try {
						commentMap = eventManagerDao.fetchCommentsForChallenges(ids);
						domain.setComments(commentMap.containsKey(id) ? commentMap.get(id) : new ArrayList<String>());
					} catch (Exception e) {
						e.printStackTrace();
						domain.setComments(new ArrayList<String>());
					}

					BeanUtils.copyProperties(domain, responseVO);
					players = eventManagerDao.fetchPlayersOfChallenges(id);

					if (players != null && !players.isEmpty()) {

						Iterator<Player> iters = players.iterator();
						int i = 0;
						while (iters.hasNext()) {
							player = iters.next();
							if (i++ == 0)
								responseVO.setPlayer1(player);
							else
								responseVO.setPlayer2(player);
						}

					}
					responseList.add(responseVO);
				}

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return responseList;
	}

	public AllChallengeResponseVO createNewChallenge(ChallengeVO challengeVO, boolean isAcceptor) throws SQLException {
		AllChallengeResponseVO responseVO = new AllChallengeResponseVO();

		long userId = challengeVO.getUserID();
		String fbUserId = challengeVO.getFbUserID();
		String playerImage = challengeVO.getPlayerImage();
		String fbPostID = null;
		String acceptorEmailId = challengeVO.getAcceptorEmailId();
		boolean userExists = false;
		try {
			userExists = eventManagerDao.userExists(acceptorEmailId);
		} catch (Exception e) {
			throw new SQLException();
		}
		// add the logic for posting to friends wall on fb

		if (userExists) {
			// post to friends wall and self wall
			if (playerImage != null && fbUserId != null) {
				fbPostID = fbClientHandler.publishPhotoToWall(fbUserId, "Rivalry Started", playerImage, false);
			}

		} else {
			if (playerImage != null && fbUserId != null) {
				fbPostID = fbClientHandler.publishPhotoToWall(fbUserId, "Rivalry Started", playerImage, false);
			}
			// for open challenge wall challenge
			// ----------------------------------//

			// -- ---//
		}
		long challengeId = 0L;
		try {
			challengeId = eventManagerDao.createNewChallenge(challengeVO, fbPostID, isAcceptor);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (challengeId > 0L) {
			String duration = challengeVO.getDuration();
			/*
			 * String jobName = Long.toString(challengeId); String group =
			 * "rivalApp"; String duration = challengeVO.getDuration();
			 * StringTokenizer strToken = new StringTokenizer(duration, ":");
			 * int i = 1; int days = 0; int hours = 0; int minutes = 0; try {
			 * while (strToken.hasMoreTokens()) {
			 * 
			 * switch (i) {
			 * 
			 * case 1: days = Integer.parseInt(strToken.nextToken()); break;
			 * case 2: hours = Integer.parseInt(strToken.nextToken()); hours =
			 * hours < 24 ? hours : hours - (hours - 24); break; case 3: minutes
			 * = Integer.parseInt(strToken.nextToken()); minutes = minutes < 60
			 * ? minutes : minutes - (minutes - 60); break; } i++; } } catch
			 * (NumberFormatException ne) { ne.printStackTrace(); }
			 * 
			 * Calendar cl = Calendar.getInstance(); cl.add(Calendar.DATE,
			 * days); cl.add(Calendar.HOUR, hours); cl.add(Calendar.MINUTE,
			 * minutes); Date when = cl.getTime(); long scheduleInvocation =
			 * rivalsAppScheduler.scheduleInvocation(jobName, group, when,
			 * rivalScheduledJob);
			 */
			/*
			 * try { eventManagerDao.createEvent(challengeId, userId, duration);
			 * } catch (Exception e) { e.printStackTrace(); }
			 */
			responseVO.setChallengeId(challengeId);
			responseVO.setCreatorId(userId);
		}
		return responseVO;
	}

	public AllChallengeResponseVO acceptChallenge(ChallengeVO challengeVO, boolean isAcceptor) {
		AllChallengeResponseVO responseVO = new AllChallengeResponseVO();

		long userId = challengeVO.getUserID();
		String fbUserId = challengeVO.getFbUserID();
		String playerImage = challengeVO.getPlayerImage();

		String fbPostID = null;
		if (playerImage != null && fbUserId != null) {
			fbPostID = fbClientHandler.publishPhotoToWall(fbUserId, challengeVO.getTopic(), playerImage, false);
		}
		String duration = challengeVO.getDuration();
		try {
			duration = eventManagerDao.getDurationForChallengId(challengeVO.getChallengeId());
		} catch (Exception e) {
			e.printStackTrace();
		}

		StringTokenizer strToken = new StringTokenizer(duration, ":");
		int i = 1;
		int days = 0;
		int hours = 0;
		int minutes = 0;
		try {
			while (strToken.hasMoreTokens()) {

				switch (i) {

				case 1:
					days = Integer.parseInt(strToken.nextToken());
					break;
				case 2:
					hours = Integer.parseInt(strToken.nextToken());
					hours = hours < 24 ? hours : hours - (hours - 24);
					break;
				case 3:
					minutes = Integer.parseInt(strToken.nextToken());
					minutes = minutes < 60 ? minutes : minutes - (minutes - 60);
					break;
				}
				i++;
			}
		} catch (NumberFormatException ne) {
			ne.printStackTrace();
		}

		Calendar cl = Calendar.getInstance();
		cl.add(Calendar.DATE, days);
		cl.add(Calendar.HOUR, hours);
		cl.add(Calendar.MINUTE, minutes);
		Date endTime = cl.getTime();

		try {
			eventManagerDao.updateAcceptedChallenge(challengeVO, fbPostID, isAcceptor, endTime);
			eventManagerDao.createEvent(challengeVO.getChallengeId());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		long challengeId = challengeVO.getChallengeId();

		String jobName = Long.toString(challengeId);
		String group = "rivalApp";

		try {
			long scheduleInvocation = rivalsAppScheduler.scheduleInvocation(jobName, group, endTime, rivalScheduledJob);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			eventManagerDao.updateEndDate(challengeId, endTime);
		} catch (Exception e) {
			e.printStackTrace();
		}

		responseVO.setStatus("INPROGRESS");
		responseVO.setCreatorId(userId);
		responseVO.setAcceptorId(userId);
		return responseVO;
	}

	public String submitComment(CommentVO commentVO) throws SQLException {

		long commentID = eventManagerDao.insertCommentOnChallenge(commentVO);

		return "Comment Submitted Successfully!";
	}

	public LikeResponseVO submitLike(int playerId,int userId,int challengeId){
		LikeResponseVO resp = new LikeResponseVO();
		String submitLike = "";
		try{
			submitLike = eventManagerDao.submitLike(playerId, userId, challengeId);
		}catch(Exception e){
			
		}
		resp.setMessage(submitLike);
		return resp;
	}
	
	
	public List<CommentsResponseVO> fetchComments(long challengeID) throws SQLException{
		return eventManagerDao.fetchComments(challengeID);
	}
	
	
	
}
