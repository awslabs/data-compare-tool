import Header from "./Header";
import Footer from "./Footer";
import { Box } from "@mui/material";

const Container = ({ children }) => {
    return (
        <>
            <Header />
            <Box
                px={{ xs: 2 }}
                py={2}
                mb="80px"
                mx={{ xs: "50px", md: "100px" }}
                sx={{
                    border: 1,
                    borderColor: "primary.main",
                    borderRadius: 2,
                    maxWidth: "1400px",
                    minWidth: "700px",
                    width: "auto",
                }}
            >
                {children}
            </Box>
            <Footer />
        </>
    );
};
export default Container;
