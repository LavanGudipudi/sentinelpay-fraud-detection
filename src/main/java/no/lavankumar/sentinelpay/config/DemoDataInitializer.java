package no.lavankumar.sentinelpay.config;

import no.lavankumar.sentinelpay.domain.Models.*;
import no.lavankumar.sentinelpay.domain.RiskScoringService;
import no.lavankumar.sentinelpay.persistence.*;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Configuration
public class DemoDataInitializer {
    @Bean ApplicationRunner seedDemoData(TransactionRepository txRepo,FraudCaseRepository caseRepo,
            RiskSignalRepository signalRepo,RiskScoringService scorer,TransactionTemplate txTemplate) {
        return args -> txTemplate.executeWithoutResult(status -> {
            if(txRepo.count()>0) return;
            Instant now=Instant.now();
            List<Transaction> demo=List.of(
              new Transaction("SP-1042","Kari Nilsen",new BigDecimal("48500"),"Nordic Electronics","NO",2,new BigDecimal("850"),true,now.minusSeconds(240)),
              new Transaction("SP-1041","Ola Berg",new BigDecimal("1290"),"Oslo Coffee Roasters","NO",180,new BigDecimal("1100"),false,now.minusSeconds(900)),
              new Transaction("SP-1039","Mina Hansen",new BigDecimal("9200"),"Travel Market","SE",4,new BigDecimal("1400"),true,now.minusSeconds(1800)),
              new Transaction("SP-1037","Erik Johansen",new BigDecimal("640"),"City Transit","NO",65,new BigDecimal("520"),false,now.minusSeconds(3600)),
              new Transaction("SP-1032","Sara Lund",new BigDecimal("18700"),"Digital Goods Hub","DK",3,new BigDecimal("2100"),true,now.minusSeconds(5400)),
              new Transaction("SP-1028","Jonas Dahl",new BigDecimal("2450"),"Grocery Market","NO",240,new BigDecimal("1800"),false,now.minusSeconds(7200)));
            for(Transaction t:demo) {
                txRepo.save(new TransactionEntity(t.id(),t.customer(),t.amountNok(),t.merchant(),t.country(),t.minutesSincePrevious(),t.usualAmountNok(),t.newDevice(),t.createdAt()));
                RiskAssessment a=scorer.assess(t);caseRepo.save(new FraudCaseEntity(t.id(),a.score(),a.level()));
                for(RiskSignal s:a.signals()) signalRepo.save(new RiskSignalEntity(t.id(),s.code(),s.title(),s.detail(),s.points()));
            }
        });
    }
}
