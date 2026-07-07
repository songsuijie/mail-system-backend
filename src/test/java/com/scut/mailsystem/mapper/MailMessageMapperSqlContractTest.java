package com.scut.mailsystem.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class MailMessageMapperSqlContractTest {

    private static final Path MAPPER_XML = Path.of(
            "src/main/resources/mapper/MailMessageMapper.xml");

    @Test
    void threadListEntryQueriesShouldOnlyUseReceivedMails() throws IOException {
        String xml = Files.readString(MAPPER_XML, StandardCharsets.UTF_8);

        String countThreads = selectBlock(xml, "countThreads");
        String selectThreadPage = selectBlock(xml, "selectThreadPage");

        assertThat(countThreads)
                .doesNotContain("WHERE (m.sender_id = #{userId} OR r.recipient_id = #{userId})")
                .contains("WHERE r.recipient_id = #{userId}");
        assertThat(selectThreadPage)
                .doesNotContain("WHERE (m.sender_id = #{userId} OR r.recipient_id = #{userId})")
                .contains("WHERE r.recipient_id = #{userId}");
    }

    @Test
    void mailDetailQueryShouldReturnThreadFieldsForReplyValidation() throws IOException {
        String xml = Files.readString(MAPPER_XML, StandardCharsets.UTF_8);

        String selectDetailByMailId = selectBlock(xml, "selectDetailByMailId");

        assertThat(selectDetailByMailId)
                .contains("m.thread_id")
                .contains("m.reply_to_mail_id");
    }

    @Test
    void threadListEntryQueriesShouldExcludeDeletedRecipientMails() throws IOException {
        String xml = Files.readString(MAPPER_XML, StandardCharsets.UTF_8);

        String countThreads = selectBlock(xml, "countThreads");
        String selectThreadPage = selectBlock(xml, "selectThreadPage");

        assertThat(countThreads).contains("AND r.deleted_flag = 0");
        assertThat(selectThreadPage).contains("AND r.deleted_flag = 0");
    }

    private static String selectBlock(String xml, String id) {
        Pattern pattern = Pattern.compile(
                "<select\\s+id=\"" + id + "\"[\\s\\S]*?</select>");
        Matcher matcher = pattern.matcher(xml);
        assertThat(matcher.find()).isTrue();
        return matcher.group();
    }
}
