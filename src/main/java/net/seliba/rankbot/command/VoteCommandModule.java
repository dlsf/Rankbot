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
        if (event.getChannel().getIdLong() != 555106694439501827L)
            return;

        User author = event.getAuthor();
        List<Member> mentions = event.getMessage().getMentionedMembers();
        if (mentions.isEmpty()) {
            event.reply(Messages.createMessage(author, "Vote", "Bitte verwende !vote @User"));
            return;
        }

        Member candidate = mentions.get(0);

        if (!isVoteDay()) {
            event.reply(Messages.createMessage(author, "Vote", "Du darfst nur am Wochendende voten!"));
        } else if (candidate.getUser().equals(author)) {
            event.reply(Messages.createMessage(author, "Vote", "Du darfst nicht für dich selbst voten!"));
        } else if (votesDao.hasVoted(author)) {
            event.reply(Messages.createMessage(author, "Vote", "Du hast bereits gevotet!"));
        } else if (isExpert(candidate)) {
            event.reply(Messages.createMessage(author, "Vote", "Dieser Nutzer ist bereits ein Experte"));
        } else {
            votesDao.vote(author, candidate.getUser());
            event.reply(Messages.createMessage(author, "Vote", "Du hast erfolgreich für "
                    + candidate.getEffectiveName() + " abgestimmt."));
        }
    }

    @JDACommand(name = "votes")
    public void viewVotes(CommandEvent event) {
        User author = event.getAuthor();
        List<Member> mentions = event.getMessage().getMentionedMembers();
        if (mentions.isEmpty()) {
            event.reply(Messages.createMessage(author, "Vote", "Du hast " + votesDao.getVotes(author) + " Votes!"));
        } else if (!mentions.get(0).equals(event.getMember()) && !isExpert(event.getMember())) {
            event.reply(Messages.createMessage(author, "Vote", "Du darfst nur deine eigenen Votes einsehen!"));
        } else {
            User candidate = mentions.get(0).getUser();
            event.reply(Messages.createMessage(author, "Vote",
                    "Der User " + candidate.getAsMention() + " hat " + votesDao.getVotes(candidate) + " Votes!"));
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
        VoteUpdateRunnable.stopVoting();
    }

}
