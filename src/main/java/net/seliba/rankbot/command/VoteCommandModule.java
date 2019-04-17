package net.seliba.rankbot.command;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.annotation.JDACommand;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.seliba.rankbot.files.VotesDao;
import net.seliba.rankbot.runnables.VoteUpdateRunnable;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

/**
 * @author Johnny_JayJay (https://www.github.com/JohnnyJayJay)
 */
@JDACommand.Module({"determineWinner", "vote", "viewVotes"})
public class VoteCommandModule {

  private final VotesDao votesDao;

  public VoteCommandModule(VotesDao votesDao) {
    this.votesDao = votesDao;
  }

  @JDACommand(name = "vote")
  public void vote(CommandEvent event) {
    if (event.getChannel().getIdLong() != 555106694439501827L) {
      return;
    }


    User author = event.getAuthor();
    List<Member> mentions = event.getMessage().getMentionedMembers();
    if (mentions.isEmpty()) {
      event.reply(Messages.createMessage(author, "Vote", "Bitte verwende !vote @User"), Messages::deleteShortly);
      return;
    }

    Member candidate = mentions.get(0);

    if (!isVoteDay()) {
      event.reply(Messages.createMessage(author, "Vote", "Du darfst nur am Wochendende voten!"), Messages::deleteShortly);
    } else if (candidate.getUser().equals(author)) {
      event.reply(Messages.createMessage(author, "Vote", "Du darfst nicht für dich selbst voten!"), Messages::deleteShortly);
    } else if (votesDao.hasVoted(author)) {
      event.reply(Messages.createMessage(author, "Vote", "Du hast bereits gevotet!"), Messages::deleteShortly);
    } else if (isExpert(candidate)) {
      event.reply(Messages.createMessage(author, "Vote", "Dieser Nutzer ist bereits ein Experte"), Messages::deleteShortly);
    } else {
      votesDao.vote(author, candidate.getUser());
      event.reply(Messages.createMessage(author, "Vote", "Du hast erfolgreich für "
          + candidate.getEffectiveName() + " abgestimmt."), Messages::deleteShortly);
    }
  }

  @JDACommand(name = "votes")
  public void viewVotes(CommandEvent event) {
    User author = event.getAuthor();
    List<Member> mentions = event.getMessage().getMentionedMembers();
    if (mentions.isEmpty()) {
      event.reply(Messages.createMessage(author, "Vote", "Du hast " + votesDao.getVotes(author) + " Votes!"), Messages::deleteShortly);
    } else if (!mentions.get(0).equals(event.getMember()) && !isExpert(event.getMember())) {
      event.reply(Messages.createMessage(author, "Vote", "Du darfst nur deine eigenen Votes einsehen!"), Messages::deleteShortly);
    } else {
      User candidate = mentions.get(0).getUser();
      event.reply(Messages.createMessage(author, "Vote", "Der User "
              + candidate.getAsMention()
              + " hat "
              + votesDao.getVotes(candidate)
              + " Votes!"), Messages::deleteShortly);
    }
  }

  private boolean isExpert(Member member) {
    return member.getRoles().stream().map(Role::getId).anyMatch("486161927765098496"::equals);
  }

  private boolean isVoteDay() {
    DayOfWeek today = LocalDate.now().getDayOfWeek();
    return today == DayOfWeek.SATURDAY || today == DayOfWeek.SUNDAY;
  }

  @JDACommand(name = "winner")
  public void determineWinner(CommandEvent event) {
    if (event.getAuthor().getIdLong() == 450632370354126858L) {
      VoteUpdateRunnable.stopVoting();
    }
  }
}
