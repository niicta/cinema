package modeloperations.impl;

import model.*;
import model.user.User;
import modeloperations.DataManager;
import modeloperations.DataUtils;
import repository.Repository;
import specifications.factory.SpecificationFactory;
import specifications.sql.SqlSpecification;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DataManagerImpl implements DataManager {
    @Inject
    private Repository<User> userRepository;
    @Inject
    private Repository<AgeLimitType> ageLimitTypeRepository;
    @Inject
    private Repository<Film> filmRepository;
    @Inject
    private  Repository<FilmType> filmTypeRepository;
    @Inject
    private Repository<Line> lineRepository;
    @Inject
    private Repository<Seance> seanceRepository;
    @Inject
    private  Repository<Seat> seatRepository;
    @Inject
    private Repository<SeatSeanceStatus> seatSeanceStatusRepository;
    @Inject
    private  Repository<SeatSeanceStatusMapper> seatSeanceStatusMapperRepository;
    @Inject
    private Repository<SeatType> seatTypeRepository;
    @Inject
    private Repository<Theater> theaterRepository;

    @Inject
    private SpecificationFactory specificationFactory;
    @Inject
    private DataUtils dataUtils;

    public void createTheater(Theater theater) {
        theaterRepository.add(theater);
        createLines(theater.getLines());
    }

    public void createFilmType(FilmType filmType) {
        filmTypeRepository.add(filmType);
    }

    public void createFilm(Film film) {
        filmRepository.add(film);
    }

    public void createSeanceForTheater(Seance seance, Theater theater) {
        bindSeanceForTheater(seance, theater);
        seanceRepository.add(seance);
    }

    public void createUser(User user){
        userRepository.add(user);
    }

    public Theater getTheater(long theaterId) {
        SqlSpecification theaterByIdSpecification = (SqlSpecification) specificationFactory.getTheaterByIdSpecification(theaterId);
        Theater theater = theaterRepository.query(theaterByIdSpecification).get(0);
        wireWithLines(theater);
        return theater;
    }

    //TODO остановился тут
    public Collection<Theater> getAllTheaters() {
        return null;
    }

    public Seance getSeance(long seanceId) {
        return null;
    }

    public Collection<Seance> getAllSeances() {
        return null;
    }

    public User getUser(long userId) {
        return null;
    }

    public void updateTheater(Theater theater) {
        theaterRepository.update(theater);
        updateLinesOrCreateNewOnes(theater);
    }

    private void createLines(Iterable<Line> lines) {
        for (Line line : lines) {
            createLine(line);
        }
    }

    private void createLine(Line line) {
        lineRepository.add(line);
        createSeats(line.getSeats());
    }

    private void createSeats(Iterable<Seat> seats) {
        for (Seat seat : seats) {
            createSeat(seat);
        }
    }

    private void createSeat(Seat seat) {
        seatRepository.add(seat);
    }

    private void bindSeanceForTheater(Seance seance, Theater theater) {
        List<Seat> seats = getSeatsForTheater(theater);
        for (Seat seat : seats) {
            createSeatSeanceStatusMapping(seat, seance);
        }
    }

    private List<Seat> getSeatsForTheater(Theater theater) {
        List<Line> lines = getLinesForTheater(theater);
        List<Seat> seats = new ArrayList<Seat>();
        for (Line line : lines) {
            seats.addAll(getSeatsForLine(line));
        }
        return seats;
    }

    private void createSeatSeanceStatusMapping(Seat seat, Seance seance) {
        SeatSeanceStatusMapper mapper = new SeatSeanceStatusMapper(seat, seance, SeatSeanceStatus.FREE);
        seatSeanceStatusMapperRepository.add(mapper);
    }

    private void wireWithLines(Theater theater) {
        List<Line> lines = getLinesForTheater(theater);
        wireWithSeats(lines);
        theater.addLines(lines);
    }

    private void wireWithSeats(List<Line> lines) {
        for (Line line : lines) {
            wireWithSeats(line);
        }
    }

    private void wireWithSeats(Line line) {
        List<Seat> seats = getSeatsForLine(line);
        line.addSeats(seats);
    }

    private void updateLinesOrCreateNewOnes(Theater theater) {
        List<Line> incomingLines = theater.getLines();
        List<Line> existingLines = getLinesForTheater(theater);
        for (Line incomingLine : incomingLines) {
            if (dataUtils.isObjectContainedInDataBase(incomingLine)) {
                lineRepository.add(incomingLine);
            } else {
                lineRepository.update(incomingLine);
            }
        }
        updateSeatsOrCreateNewOnes(theater);
    }

    private void updateSeatsOrCreateNewOnes(Theater theater) {
        List<Seat> seats = new ArrayList<Seat>();
        for (Line line : theater.getLines()) {
            seats.addAll(line.getSeats());
        }
        for (Seat seat : seats) {
            if (dataUtils.isObjectContainedInDataBase(seat)) {
                seatRepository.add(seat);
            } else {
                seatRepository.update(seat);
            }
        }
    }

    private List<Line> getLinesForTheater(Theater theater) {
        SqlSpecification lineByTheaterIdSpecification = (SqlSpecification) specificationFactory.getLineByTheaterIdSpecification(theater.getTheaterID());
        List<Line> lines = lineRepository.query(lineByTheaterIdSpecification);
        return lines;
    }

    private List<Seat> getSeatsForLine(Line line) {
        SqlSpecification seatByLineIdSpecification = (SqlSpecification) specificationFactory.getSeatByLineIdSpecification(line.getLineID());
        List<Seat> seats = seatRepository.query(seatByLineIdSpecification);
        return seats;
    }
}